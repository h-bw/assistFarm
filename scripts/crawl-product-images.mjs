import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'

/**
 * 图片爬取脚本（Image scraping script）
 * - 输入：从 SQL 文件里抽取 products 的产品名称
 * - 处理：按产品名称创建文件夹，去网络搜索图片并下载
 * - 输出：默认写入 ./.downloaded-images/<产品名称>/<序号>.<扩展名>
 *
 * 注意：爬取（scraping）公开搜索结果可能受站点策略/网络环境影响，如被限流请降低并发或增加重试间隔。
 */

function parseArgs(argv) {
  const args = {
    sql: path.resolve('sql/huacai-assisting.sql'),
    out: path.resolve('.downloaded-images'),
    per: 5,
    limitProducts: 0,
    start: 0,
    concurrency: 3,
    querySuffix: ' 实物 高清',
    timeoutMs: 20000,
    retry: 2,
    safeSearch: 1,
    // 为了尽量补齐 per 张，会多取一些候选 URL
    overfetchFactor: 4,
    // 失败报告输出路径（JSON）
    report: path.resolve('.downloaded-images', 'crawl-report.json'),
    // 增量补抓：只处理失败产品（从报告里读取）
    resumeReport: '',
    onlyFailed: 0,
    // 增量补抓时：只补缺的图片（根据文件夹内现有图片数决定起始序号）
    fillMissing: 1
  }

  for (let i = 0; i < argv.length; i += 1) {
    const key = argv[i]
    const next = argv[i + 1]
    if (!key?.startsWith('--')) continue
    if (next === undefined || next.startsWith('--')) {
      args[key.slice(2)] = true
      continue
    }
    const k = key.slice(2)
    const v = next
    i += 1
    if (['per', 'limitProducts', 'start', 'concurrency', 'timeoutMs', 'retry', 'safeSearch'].includes(k)) {
      args[k] = Number(v)
    } else {
      args[k] = v
    }
  }

  return args
}

function sanitizeFolderName(input) {
  const raw = String(input ?? '').trim()
  // Windows 文件夹名非法字符：<>:"/\|?* 以及控制字符
  const cleaned = raw
    .replace(/[<>:"/\\|?*\u0000-\u001F]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .slice(0, 80)
  return cleaned || '未命名商品'
}

function sha1(input) {
  return createHash('sha1').update(String(input)).digest('hex')
}

function guessExtFromContentType(contentType) {
  const ct = String(contentType || '').toLowerCase()
  if (ct.includes('image/jpeg')) return 'jpg'
  if (ct.includes('image/png')) return 'png'
  if (ct.includes('image/webp')) return 'webp'
  if (ct.includes('image/gif')) return 'gif'
  if (ct.includes('image/bmp')) return 'bmp'
  if (ct.includes('image/svg')) return 'svg'
  return ''
}

function guessExtFromUrl(url) {
  try {
    const u = new URL(url)
    const p = u.pathname.toLowerCase()
    const m = p.match(/\.(jpg|jpeg|png|webp|gif|bmp|svg)$/i)
    if (m?.[1]) return m[1] === 'jpeg' ? 'jpg' : m[1]
  } catch {
    // ignore
  }
  return ''
}

function splitSqlValuesTuple(sqlTupleText) {
  // 解析类似：('id','name',39.9,'/img.png',...)
  // 仅处理单条 INSERT 的 VALUES(...) 内容，不跨行
  const s = String(sqlTupleText || '').trim()
  if (!s.startsWith('(') || !s.endsWith(')')) return []

  const out = []
  let cur = ''
  let inString = false
  for (let i = 1; i < s.length - 1; i += 1) {
    const ch = s[i]
    if (inString) {
      if (ch === '\\' && i + 1 < s.length - 1) {
        cur += s[i + 1]
        i += 1
        continue
      }
      if (ch === "'") {
        // SQL 里 '' 表示单引号
        if (s[i + 1] === "'") {
          cur += "'"
          i += 1
          continue
        }
        inString = false
        continue
      }
      cur += ch
      continue
    }

    if (ch === "'") {
      inString = true
      continue
    }

    if (ch === ',') {
      out.push(cur.trim())
      cur = ''
      continue
    }
    cur += ch
  }
  out.push(cur.trim())
  return out.map(v => (v.toUpperCase() === 'NULL' ? '' : v))
}

async function readProductNamesFromSql(sqlPath) {
  const text = await fs.readFile(sqlPath, 'utf8')
  const lines = text.split(/\r?\n/)

  const names = []
  for (const line of lines) {
    if (!/INSERT\s+INTO\s+`?products`?\s+VALUES\s*\(/i.test(line)) continue
    const m = line.match(/VALUES\s*(\(.+\))\s*;?\s*$/i)
    if (!m?.[1]) continue
    const values = splitSqlValuesTuple(m[1])
    // 表结构里第二列是 name
    const name = values[1]
    if (name) names.push(String(name))
  }

  return Array.from(new Set(names))
}

async function findLatestReportFile(outRoot) {
  try {
    const entries = await fs.readdir(outRoot, { withFileTypes: true })
    const reportFiles = entries
      .filter(e => e.isFile() && /^crawl-report(\..+)?\.json$/i.test(e.name))
      .map(e => path.join(outRoot, e.name))
    if (reportFiles.length === 0) return ''

    let best = reportFiles[0]
    let bestMtime = 0
    for (const f of reportFiles) {
      try {
        const st = await fs.stat(f)
        const m = Number(st.mtimeMs || 0)
        if (m > bestMtime) {
          bestMtime = m
          best = f
        }
      } catch {
        // ignore
      }
    }
    return best
  } catch {
    return ''
  }
}

async function fetchWithTimeout(url, options, timeoutMs) {
  const controller = new AbortController()
  const id = setTimeout(() => controller.abort(), timeoutMs)
  try {
    const res = await fetch(url, { ...options, signal: controller.signal })
    return res
  } finally {
    clearTimeout(id)
  }
}

async function getDuckDuckGoVqd(query, { timeoutMs }) {
  const u = `https://duckduckgo.com/?q=${encodeURIComponent(query)}&iax=images&ia=images`
  const res = await fetchWithTimeout(
    u,
    {
      headers: {
        'user-agent':
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123 Safari/537.36',
        accept: 'text/html,application/xhtml+xml'
      }
    },
    timeoutMs
  )
  if (!res.ok) throw new Error(`获取 DuckDuckGo 页面失败：${res.status}`)
  const html = await res.text()

  // 常见形式：vqd='...'; 或 vqd="..."
  const m = html.match(/vqd=['"]([^'"]+)['"]/)
  if (!m?.[1]) throw new Error('未能解析 vqd（可能被限流或页面结构变化）')
  return m[1]
}

async function searchDuckDuckGoImages(query, { safeSearch, timeoutMs, maxResults }) {
  const vqd = await getDuckDuckGoVqd(query, { timeoutMs })

  let nextUrl = `https://duckduckgo.com/i.js?o=json&q=${encodeURIComponent(query)}&vqd=${encodeURIComponent(
    vqd
  )}&p=${safeSearch ? 1 : -1}`

  const results = []
  const seen = new Set()
  while (nextUrl && results.length < maxResults) {
    const res = await fetchWithTimeout(
      nextUrl,
      {
        headers: {
          'user-agent':
            'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123 Safari/537.36',
          accept: 'application/json,text/plain,*/*',
          referer: 'https://duckduckgo.com/'
        }
      },
      timeoutMs
    )

    if (!res.ok) throw new Error(`DuckDuckGo 图片接口失败：${res.status}`)
    const json = await res.json()
    const arr = Array.isArray(json?.results) ? json.results : []
    for (const it of arr) {
      const url = it?.image
      if (!url) continue
      const key = sha1(url)
      if (seen.has(key)) continue
      seen.add(key)
      results.push(url)
      if (results.length >= maxResults) break
    }

    const next = json?.next
    if (!next) break
    nextUrl = next.startsWith('http') ? next : `https://duckduckgo.com${next}`
  }

  return results
}

async function ensureDir(dir) {
  await fs.mkdir(dir, { recursive: true })
}

async function exists(p) {
  try {
    await fs.access(p)
    return true
  } catch {
    return false
  }
}

async function countExistingImages(folder) {
  try {
    const entries = await fs.readdir(folder, { withFileTypes: true })
    let count = 0
    for (const e of entries) {
      if (!e.isFile()) continue
      const n = e.name.toLowerCase()
      if (!/^\d{2}\.(jpg|jpeg|png|webp|gif|bmp|svg)$/.test(n)) continue
      count += 1
    }
    return count
  } catch {
    return 0
  }
}

async function downloadImage(url, filePath, { timeoutMs, retry }) {
  if (await exists(filePath)) return { skipped: true }

  let lastErr
  for (let attempt = 0; attempt <= retry; attempt += 1) {
    try {
      const res = await fetchWithTimeout(
        url,
        {
          headers: {
            'user-agent':
              'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123 Safari/537.36'
          }
        },
        timeoutMs
      )
      if (!res.ok) throw new Error(`下载失败：${res.status}`)
      const buf = Buffer.from(await res.arrayBuffer())
      await fs.writeFile(filePath, buf)
      return { skipped: false }
    } catch (e) {
      lastErr = e
      // 简单退避
      const wait = 400 * Math.pow(2, attempt)
      await new Promise(r => setTimeout(r, wait))
    }
  }
  throw lastErr
}

async function probeImageExt(url, { timeoutMs, retry }) {
  // 尽量通过 HEAD 获取 content-type 推断扩展名；不支持则返回空串
  let lastErr
  for (let attempt = 0; attempt <= retry; attempt += 1) {
    try {
      const res = await fetchWithTimeout(
        url,
        {
          method: 'HEAD',
          headers: {
            'user-agent':
              'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123 Safari/537.36'
          }
        },
        timeoutMs
      )
      if (!res.ok) return ''
      const ct = res.headers.get('content-type') || ''
      return guessExtFromContentType(ct)
    } catch (e) {
      lastErr = e
      const wait = 300 * Math.pow(2, attempt)
      await new Promise(r => setTimeout(r, wait))
    }
  }
  void lastErr
  return ''
}

async function mapLimit(items, limit, worker) {
  const ret = []
  let idx = 0
  const run = async () => {
    while (idx < items.length) {
      const cur = idx
      idx += 1
      ret[cur] = await worker(items[cur], cur)
    }
  }
  const runners = Array.from({ length: Math.max(1, limit) }, () => run())
  await Promise.all(runners)
  return ret
}

async function main() {
  const args = parseArgs(process.argv.slice(2))
  const sqlPath = path.resolve(args.sql)
  const outRoot = path.resolve(args.out)

  let names = await readProductNamesFromSql(sqlPath)

  // 增量补抓：从上次报告里拿失败清单
  if (args.resumeReport) {
    try {
      const resumeText = await fs.readFile(path.resolve(args.resumeReport), 'utf8')
      const resumeJson = JSON.parse(resumeText)
      const failed = Array.isArray(resumeJson?.failedProducts) ? resumeJson.failedProducts : []
      const failedNames = failed.map(x => String(x?.name || '')).filter(Boolean)
      if (failedNames.length > 0) {
        names = failedNames
        args.onlyFailed = 1
      }
    } catch (e) {
      console.error(`[Warn] 读取 resumeReport 失败：${String(e?.message || e)}`)
    }
  } else if (args.onlyFailed) {
    // 用户只想处理失败产品但没给报告：尝试自动找最新报告
    const latest = await findLatestReportFile(outRoot)
    if (latest) {
      try {
        const resumeText = await fs.readFile(latest, 'utf8')
        const resumeJson = JSON.parse(resumeText)
        const failed = Array.isArray(resumeJson?.failedProducts) ? resumeJson.failedProducts : []
        const failedNames = failed.map(x => String(x?.name || '')).filter(Boolean)
        if (failedNames.length > 0) {
          names = failedNames
          args.resumeReport = latest
        }
      } catch (e) {
        console.error(`[Warn] 自动读取最新报告失败：${String(e?.message || e)}`)
      }
    } else {
      console.error('[Warn] 未找到 crawl-report*.json，无法仅处理失败产品；将按全量处理。')
    }
  }

  const slice = names.slice(args.start, args.limitProducts > 0 ? args.start + args.limitProducts : undefined)

  await ensureDir(outRoot)

  console.log(`[Info] SQL：${sqlPath}`)
  console.log(`[Info] 输出目录：${outRoot}`)
  console.log(`[Info] 发现产品数：${names.length}，本次处理：${slice.length}`)
  console.log(
    `[Info] 每个产品目标：${args.per} 张，并发：${args.concurrency}，重试：${args.retry}，安全搜索：${args.safeSearch ? '开' : '关'}`
  )
  console.log(`[Info] 失败报告：${path.resolve(args.report)}`)
  if (args.onlyFailed) {
    console.log('[Info] 模式：仅处理失败产品（增量补抓）')
  }

  const summary = { products: slice.length, ok: 0, failed: 0, downloaded: 0, skipped: 0 }
  const report = {
    generatedAt: new Date().toISOString(),
    args,
    summary,
    failedProducts: []
  }

  await mapLimit(slice, args.concurrency, async (name, productIndex) => {
    const folderName = sanitizeFolderName(name)
    const folder = path.join(outRoot, folderName)
    await ensureDir(folder)

    const query = `${name}${args.querySuffix || ''}`.trim()
    process.stdout.write(`\n[${productIndex + 1}/${slice.length}] 搜索：${query}\n`)

    try {
      const want = Math.max(1, args.per)
      const overfetch = Math.max(want, want * Math.max(1, Number(args.overfetchFactor) || 1))

      const urls = await searchDuckDuckGoImages(query, {
        safeSearch: args.safeSearch,
        timeoutMs: args.timeoutMs,
        maxResults: overfetch
      })

      if (urls.length === 0) throw new Error('未搜索到图片结果')

      let saved = 0
      const imageFailures = []

      // 增量补抓：从现有图片数量开始补
      let startIndex = 0
      if (args.onlyFailed && args.fillMissing) {
        const existed = await countExistingImages(folder)
        startIndex = Math.max(0, Math.min(existed, want))
        saved = startIndex
        if (startIndex >= want) {
          process.stdout.write(`- 已满足目标：已有 ${startIndex}/${want} 张，跳过\n`)
          summary.ok += 1
          return
        }
        process.stdout.write(`- 增量补抓：已有 ${startIndex}/${want} 张，从 ${startIndex + 1} 开始补齐\n`)
      }

      for (let u = 0; u < urls.length && saved < want; u += 1) {
        const url = urls[u]
        const seq = String(saved + 1).padStart(2, '0')

        let ext = guessExtFromUrl(url)
        if (!ext) {
          ext = await probeImageExt(url, { timeoutMs: args.timeoutMs, retry: Math.max(0, args.retry - 1) })
        }
        if (!ext) ext = 'jpg'

        const fileName = `${seq}.${ext}`
        const filePath = path.join(folder, fileName)
        try {
          const r = await downloadImage(url, filePath, { timeoutMs: args.timeoutMs, retry: args.retry })
          if (r.skipped) summary.skipped += 1
          else summary.downloaded += 1
          saved += 1
          process.stdout.write(`- ${r.skipped ? '跳过' : '已下载'}：${fileName}\n`)
        } catch (e) {
          const msg = String(e?.message || e)
          imageFailures.push({ url, error: msg })
          process.stdout.write(`- 失败（继续尝试下一张）：${msg}\n`)
        }
      }

      if (saved >= want) {
        summary.ok += 1
      } else {
        summary.failed += 1
        report.failedProducts.push({
          name,
          folder: folderName,
          query,
          wanted: want,
          saved,
          failures: imageFailures.slice(0, 15)
        })
      }
    } catch (e) {
      summary.failed += 1
      report.failedProducts.push({
        name,
        folder: folderName,
        query,
        wanted: Math.max(1, args.per),
        saved: 0,
        failures: [{ url: null, error: String(e?.message || e) }]
      })
      process.stdout.write(`- 失败：${String(e?.message || e)}\n`)
    }
  })

  // 写失败报告（便于二次补抓）
  try {
    await ensureDir(path.dirname(path.resolve(args.report)))
    await fs.writeFile(path.resolve(args.report), JSON.stringify(report, null, 2), 'utf8')
  } catch (e) {
    console.error(`[Warn] 写入失败报告失败：${String(e?.message || e)}`)
  }

  console.log(`\n[Done] 产品：${summary.products}，成功：${summary.ok}，失败：${summary.failed}`)
  console.log(`[Done] 下载：${summary.downloaded}，跳过：${summary.skipped}`)
  console.log(`[Done] 输出目录：${outRoot}`)
}

main().catch(e => {
  console.error(`[Fatal] ${String(e?.stack || e)}`)
  process.exitCode = 1
})

