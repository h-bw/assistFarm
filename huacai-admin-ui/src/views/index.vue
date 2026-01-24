<template>
    <div class="home-service-container">
        <!-- 主横幅 -->
        <div class="main-banner">
            <el-card shadow="never" class="transparent-card">
                <div class="banner-content">
                    <div class="banner-text">
                        <h1>助农扶贫后台管理</h1>
                        <p class="sub-title">助力乡村振兴 · 共同富裕</p>
                        <el-divider></el-divider>
                        <p class="banner-description">
                            通过数字化手段提升农业扶贫工作的效率和管理水平，
                            连接农户与消费者，打造透明、高效的助农平台。
                        </p>
                    </div>
                </div>
            </el-card>
        </div>

        <!-- 平台优势 -->
        <div class="advantage-section">
            <h2 class="section-title">
                <el-icon>
                    <Medal/>
                </el-icon>
                平台优势
            </h2>
            <el-row :gutter="20">
                <el-col :xs="12" :sm="8" :md="8" :lg="8">
                    <div class="advantage-card">
                        <div class="advantage-icon">
                            <el-icon>
                                <Finished/>
                            </el-icon>
                        </div>
                        <h4>精准帮扶</h4>
                        <p>通过数据分析实现精准扶贫</p>
                    </div>
                </el-col>
                <el-col :xs="12" :sm="8" :md="8" :lg="8">
                    <div class="advantage-card">
                        <div class="advantage-icon">
                            <el-icon>
                                <Connection/>
                            </el-icon>
                        </div>
                        <h4>信息透明</h4>
                        <p>全流程信息公开透明</p>
                    </div>
                </el-col>
                <el-col :xs="12" :sm="8" :md="8" :lg="8">
                    <div class="advantage-card">
                        <div class="advantage-icon">
                            <el-icon>
                                <DataLine/>
                            </el-icon>
                        </div>
                        <h4>高效管理</h4>
                        <p>数字化管理提升工作效率</p>
                    </div>
                </el-col>
            </el-row>
        </div>

        <!-- 平台介绍 -->
        <div class="intro-section">
            <el-row :gutter="30">
                <el-col :xs="24" :md="12">
                    <div class="intro-card">
                        <h3>平台简介</h3>
                        <p>
                            助农扶贫系统是一个专门为农业扶贫开发的数字化管理平台。
                            系统集成了农户信息管理、农产品展销、扶贫项目跟踪、数据统计分析等功能，
                            旨在通过信息化手段提升扶贫工作的精准度和效率。
                        </p>
                    </div>
                </el-col>
                <el-col :xs="24" :md="12">
                    <div class="intro-card">
                        <h3>愿景使命</h3>
                        <p>
                            我们的愿景是通过科技手段助力乡村振兴，实现共同富裕。
                            使命是连接农户与市场，打通扶贫最后一公里，让每一份爱心都能精准传递，
                            让每一次帮扶都能产生实效。
                        </p>
                    </div>
                </el-col>
            </el-row>
        </div>
    </div>
</template>


<script setup>
import {Connection, DataLine, Finished, Medal} from "@element-plus/icons-vue";
</script>

<style lang="scss" scoped>
.home-service-container {
    padding: 20px;
    background-color: #f5f7fa;

    .transparent-card {
        background-color: transparent;
        border: none;
    }

    .main-banner {
        margin-bottom: 40px;

        .banner-content {
            display: flex;
            align-items: center;
            padding: 30px;
            background: linear-gradient(135deg, #5B86E5 0%, #36D1DC 100%);
            border-radius: 8px;
            color: white;

            .banner-text {
                flex: 1;
                padding-right: 40px;

                h1 {
                    font-size: 36px;
                    color: white;
                    margin-bottom: 15px;
                }

                .sub-title {
                    font-size: 18px;
                    color: rgba(255, 255, 255, 0.8);
                    margin-bottom: 20px;
                }

                .banner-description {
                    font-size: 16px;
                    line-height: 1.8;
                    color: rgba(255, 255, 255, 0.8);
                    margin-bottom: 30px;
                }

                .banner-actions {
                    display: flex;
                    gap: 20px;
                    margin-top: 30px;
                }
            }

            .banner-image {
                flex: 1;
                text-align: center;

                img {
                    max-width: 100%;
                    border-radius: 4px;
                    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
                }
            }
        }
    }

    .section-title {
        text-align: center;
        font-size: 28px;
        color: #303133;
    }

    .service-section {
        margin-bottom: 50px;

        .service-card {
            background: white;
            padding: 30px 20px;
            border-radius: 8px;
            text-align: center;
            box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
            height: 100%;
            transition: transform 0.3s;
            display: flex;
            flex-direction: column;
            align-items: center;

            &:hover {
                transform: translateY(-5px);
                box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
            }

            .service-icon {
                font-size: 48px;
                margin-bottom: 20px;
            }

            h3 {
                font-size: 20px;
                margin-bottom: 15px;
                color: #303133;
            }

            p {
                color: #606266;
                line-height: 1.6;
                margin-bottom: 20px;
            }
        }
    }

    .advantage-section {
        margin: 50px 0;

        .advantage-card {
            background: white;
            padding: 25px 15px;
            border-radius: 8px;
            text-align: center;
            box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
            height: 100%;
            margin-bottom: 20px;

            .advantage-icon {
                font-size: 36px;
                margin-bottom: 15px;
                color: #5B86E5;
            }

            h4 {
                font-size: 18px;
                margin-bottom: 10px;
                color: #303133;
            }

            p {
                color: #909399;
                font-size: 14px;
            }
        }
    }

    .intro-section {
        margin: 50px 0;

        .intro-card {
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

            h3 {
                font-size: 22px;
                margin-bottom: 20px;
                color: #303133;
                position: relative;
                padding-bottom: 10px;

                &::after {
                    content: '';
                    position: absolute;
                    bottom: 0;
                    left: 0;
                    width: 50px;
                    height: 3px;
                    background: #409EFF;
                    border-radius: 2px;
                }
            }

            p {
                color: #606266;
                line-height: 1.8;
                font-size: 16px;
            }
        }
    }
}

@media (max-width: 768px) {
    .home-service-container {
        .main-banner {
            .banner-content {
                flex-direction: column;

                .banner-text {
                    padding-right: 0;
                    margin-bottom: 30px;
                }
            }
        }

        .service-section,
        .advantage-section {
            .el-col {
                margin-bottom: 20px;
            }
        }
    }
}
</style>
