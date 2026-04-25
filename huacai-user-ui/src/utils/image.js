const IMAGE_ASSET_VERSION = '20260330-1'
const PRODUCT_ID_FOLDER_PATTERN = /^(p\d{12,}|[a-f0-9]{32})$/i

const normalizeDownloadedImagePath = (value) => {
  if (!value) return ''

  let normalized = String(value).trim()
  if (!normalized) return ''

  if (normalized.startsWith('/.downloaded-images/')) {
    normalized = normalized.replace('/.downloaded-images/', '/downloaded-images/')
  }

  if (!normalized.startsWith('/downloaded-images/')) {
    return normalized
  }

  if (normalized.startsWith('/downloaded-images/by-id/')) {
    return normalized
  }

  const pathMatch = normalized.match(/^\/downloaded-images\/([^/]+)\/(.+)$/)
  if (!pathMatch) {
    return normalized
  }

  const [, folderName, assetPath] = pathMatch
  if (!PRODUCT_ID_FOLDER_PATTERN.test(decodeURIComponent(folderName))) {
    return normalized
  }

  return `/downloaded-images/by-id/${folderName}/${assetPath}`
}

const appendVersion = (url) => {
  if (!url) return ''
  const encoded = encodeURI(url)
  return encoded.includes('?')
    ? `${encoded}&_imgv=${IMAGE_ASSET_VERSION}`
    : `${encoded}?_imgv=${IMAGE_ASSET_VERSION}`
}

export const resolveImageUrl = (image, baseUrl = '') => {
  if (!image) return ''

  const value = normalizeDownloadedImagePath(image)
  if (!value) return ''

  if (/^https?:\/\//i.test(value)) return appendVersion(value)
  if (value.startsWith('/downloaded-images/')) return appendVersion(value)
  return appendVersion(`${baseUrl}${value}`)
}

export const normalizeRichTextImagePaths = (html, baseUrl = '') => {
  if (!html) return ''

  return String(html).replace(/src=(['"])([^'"]+)\1/gi, (fullMatch, quote, src) => {
    const normalizedSrc = resolveImageUrl(src, baseUrl)
    return `src=${quote}${normalizedSrc}${quote}`
  })
}
