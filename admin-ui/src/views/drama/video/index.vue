<template>
  <div class="app-container video-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-form :model="queryParams" ref="queryRef" :inline="true" class="toolbar-form">
        <el-form-item label="标题" prop="title">
          <el-input v-model="queryParams.title" placeholder="请输入视频标题" clearable style="width:200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增视频</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" stripe class="video-table" :header-cell-style="headerStyle">
      <el-table-column label="ID" prop="id" width="80" align="center">
        <template #default="{ row }">
          <span class="id-badge">#{{ row.id }}</span>
        </template>
      </el-table-column>

      <el-table-column label="封面" width="100" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.cover"
            :src="row.cover"
            :preview-src-list="[row.cover]"
            preview-teleported
            class="cover-thumb"
            fit="cover"
          >
            <template #error>
              <div class="cover-empty" title="封面加载失败">无</div>
            </template>
            <template #placeholder>
              <div class="cover-empty">无</div>
            </template>
          </el-image>
          <div v-else class="cover-empty" title="未设置封面">无</div>
        </template>
      </el-table-column>

      <el-table-column label="短剧信息" min-width="220">
        <template #default="{ row }">
          <div class="drama-title">{{ row.title }}</div>
          <div class="drama-meta">
            <el-tag size="small" effect="light" :type="row.status === 1 ? 'success' : 'info'" class="status-tag">
              <el-icon class="status-icon">
                <CircleCheck v-if="row.status === 1" />
                <Hide v-else />
              </el-icon>
              <span>{{ row.status === 1 ? '上架' : '下架' }}</span>
            </el-tag>
            <el-tag v-if="row.is_recommend === 1" size="small" type="warning" effect="light" class="rec-tag">
              <el-icon class="rec-icon"><Star /></el-icon>
              <span>推荐</span>
            </el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="集数" width="110" align="center">
        <template #default="{ row }">
          <div class="ep-badge">
            <span class="ep-num">{{ row.seriesCount || 0 }}</span>
            <span class="ep-unit">集</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="权重" width="100" align="center">
        <template #default="{ row }">
          <span class="weigh-badge" :class="weighClass(row.weigh)">{{ row.weigh ?? 0 }}</span>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="180" align="center">
        <template #default="{ row }">
          <div class="time-cell">
            <span class="time-abs">{{ row.createTime ? row.createTime.slice(0,10) : '—' }}</span>
            <span v-if="row.createTime" class="time-rel">{{ relativeTime(row.createTime) }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" :icon="VideoPlay" @click="openEpisodes(row)">分集</el-button>
          <el-divider direction="vertical" />
          <el-button link type="primary" size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-divider direction="vertical" />
          <el-button link type="danger" size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>

      <template #empty>
        <div class="empty-state">
          <el-icon class="empty-icon"><VideoPlay /></el-icon>
          <div class="empty-text">暂无视频</div>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增第一部短剧</el-button>
        </div>
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑视频弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      width="640px"
      append-to-body
      class="ep-form-dialog"
      :show-close="false"
    >
      <template #header>
        <div class="ep-dialog-header video-dialog-header">
          <div class="ep-dialog-title">
            <el-icon class="ep-dialog-icon"><Film /></el-icon>
            <span>{{ dialog.title }}</span>
            <el-tag v-if="form.id" effect="light" type="primary" round size="small" class="ep-dialog-tag">
              #{{ form.id }}
            </el-tag>
          </div>
          <el-icon class="ep-dialog-close" @click="dialog.visible = false"><Close /></el-icon>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="ep-form-tabs">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="92px" class="ep-form">
            <!-- 分组：基础信息 -->
            <div class="ep-section">
              <div class="ep-section-title"><span class="ep-bar"></span>基础信息</div>
              <el-form-item label="默认标题" prop="title">
                <el-input v-model="form.title" placeholder="中文标题（必填，作为多语言回退）" />
              </el-form-item>
              <el-form-item label="默认描述">
                <el-input v-model="form.description" type="textarea" :rows="3" placeholder="中文描述（作为多语言回退）" />
              </el-form-item>
            </div>

            <!-- 分组：封面图（高亮） -->
            <div class="ep-section ep-section--video">
              <div class="ep-section-title">
                <span class="ep-bar"></span>封面图
                <el-tag v-if="form.cover" type="success" size="small" effect="light" round>已设置</el-tag>
                <el-tag v-else type="info" size="small" effect="light" round>未设置</el-tag>
              </div>
              <el-form-item label="封面图" prop="cover">
                <div class="video-cover-uploader">
                  <el-upload
                    action="#"
                    :show-file-list="false"
                    :before-upload="beforeUpload"
                    :http-request="(opt) => handleUpload(opt.file)"
                    accept="image/*"
                  >
                    <div v-if="form.cover" class="video-cover-wrap">
                      <el-image :src="form.cover" class="video-cover-img" fit="cover" />
                      <div class="video-cover-mask">
                        <el-icon><Edit /></el-icon><span>更换封面</span>
                      </div>
                    </div>
                    <div v-else class="upload-placeholder video-cover-placeholder">
                      <el-icon class="upload-icon"><Plus /></el-icon>
                      <span>点击上传封面</span>
                      <span class="placeholder-tip">建议 9:16 竖屏，≤ 5MB</span>
                    </div>
                  </el-upload>
                  <el-button
                    v-if="form.cover"
                    link
                    type="danger"
                    size="small"
                    :icon="Delete"
                    class="cover-clear-btn"
                    @click="form.cover = ''"
                  >移除封面</el-button>
                </div>
              </el-form-item>
              <el-form-item label="或粘贴URL">
                <el-input v-model="form.cover" placeholder="直接粘贴图片 URL（OSS 或外链均可）" clearable />
              </el-form-item>
            </div>

            <!-- 分组：上架与排序 -->
            <div class="ep-section">
              <div class="ep-section-title"><span class="ep-bar"></span>上架与排序</div>
              <el-form-item label="上架状态">
                <el-radio-group v-model="form.status" class="ep-segment">
                  <el-radio-button :value="1">
                    <el-icon><CircleCheck /></el-icon> 上架
                  </el-radio-button>
                  <el-radio-button :value="0">
                    <el-icon><Hide /></el-icon> 下架
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="推荐展示">
                <el-switch
                  v-model="form.is_recommend"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="推荐到发现页"
                  inactive-text="不推荐"
                  inline-prompt
                  style="--el-switch-on-color: #5048e5"
                />
                <span class="ep-hint">推荐的剧集会优先展示在首页发现栏</span>
              </el-form-item>
              <el-form-item label="排序权重">
                <el-input-number v-model="form.weigh" :min="0" :max="9999" style="width:160px" />
                <span class="ep-hint">数字越大排越前（推荐的排在非推荐前面）</span>
              </el-form-item>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- 每个语言 tab -->
        <el-tab-pane
          v-for="lang in supportedLangs"
          :key="lang.code"
          :label="lang.label"
          :name="lang.code"
        >
          <el-form label-width="92px" class="ep-form">
            <div class="ep-section">
              <div class="ep-section-title">
                <span class="ep-bar"></span>{{ lang.label }} 内容
              </div>
              <el-form-item label="标题">
                <el-input
                  v-model="i18nForm[lang.code].title"
                  :placeholder="`${lang.label}标题`"
                />
              </el-form-item>
              <el-form-item label="描述">
                <el-input
                  v-model="i18nForm[lang.code].desc"
                  type="textarea"
                  :rows="4"
                  :placeholder="`${lang.label}描述`"
                />
              </el-form-item>
            </div>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <div class="ep-dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" :icon="Check" @click="submitForm">确定保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分集管理抽屉 -->
    <el-drawer
      v-model="epDrawer.visible"
      :title="`《${epDrawer.videoTitle}》— 分集管理`"
      size="1060px"
      destroy-on-close
    >
      <!-- 工具栏 + 统计概览 -->
      <div class="ep-toolbar">
        <div class="ep-toolbar-left">
          <el-button type="primary" :icon="Plus" @click="openEpForm()">添加分集</el-button>
          <el-button :icon="Setting" @click="openBatchSet()">整部剧设置</el-button>
          <el-button :icon="Refresh" circle @click="loadEpisodes" />
        </div>
        <div class="ep-stats">
          <div class="ep-stat">
            <span class="ep-stat-num">{{ episodes.length }}</span>
            <span class="ep-stat-label">总集数</span>
          </div>
          <div class="ep-stat ep-stat--free">
            <span class="ep-stat-num">{{ epFreeCount }}</span>
            <span class="ep-stat-label">免费</span>
          </div>
          <div class="ep-stat ep-stat--paid">
            <span class="ep-stat-num">{{ epPaidCount }}</span>
            <span class="ep-stat-label">付费</span>
          </div>
          <div class="ep-stat ep-stat--done">
            <span class="ep-stat-num">{{ epTranscodedCount }}</span>
            <span class="ep-stat-label">已转码</span>
          </div>
        </div>
      </div>

      <el-table
        v-loading="epLoading"
        :data="episodes"
        size="small"
        stripe
        class="ep-table"
        :header-cell-style="headerStyle"
      >
        <el-table-column label="集数" prop="episodeNum" width="90" align="center" sortable>
          <template #default="{ row }">
            <span class="ep-num-badge">#{{ row.episodeNum }}</span>
          </template>
        </el-table-column>

        <el-table-column label="标题" prop="title" min-width="220" show-overflow-tooltip sortable>
          <template #default="{ row }">
            <div class="ep-title-cell">
              <span class="ep-title">{{ row.title || '—' }}</span>
              <span class="ep-title-sub">默认标题</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="时长" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.duration" class="ep-duration">
              <el-icon><Clock /></el-icon>
              {{ formatDuration(row.duration) }}
            </span>
            <span v-else class="ep-empty">—</span>
          </template>
        </el-table-column>

        <el-table-column label="解锁" width="80" align="center">
          <template #default="{ row }">
            <span class="ep-chip" :class="row.isFree ? 'ep-chip--free' : 'ep-chip--paid'">
              {{ row.isFree ? '免费' : '付费' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'info'" effect="light" size="small" class="ep-status-tag">
              <el-icon class="ep-status-icon">
                <CircleCheck v-if="row.status" />
                <Hide v-else />
              </el-icon>
              <span>{{ row.status ? '显示' : '隐藏' }}</span>
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="转码" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.transcodeStatus === 'done'" class="ep-trans-chip ep-trans-chip--done">
              <el-icon><CircleCheck /></el-icon>已转码
            </span>
            <span v-else-if="row.transcodeStatus === 'processing'" class="ep-trans-chip ep-trans-chip--processing">
              <el-icon class="ep-spin"><Loading /></el-icon>转码中
            </span>
            <span v-else-if="row.transcodeStatus === 'pending'" class="ep-trans-chip ep-trans-chip--pending">
              <el-icon><Clock /></el-icon>排队中
            </span>
            <el-tooltip v-else-if="row.transcodeStatus === 'failed'" :content="row.transcodeMsg || '转码失败'" placement="top">
              <span class="ep-trans-chip ep-trans-chip--failed">
                <el-icon><WarningFilled /></el-icon>失败
              </span>
            </el-tooltip>
            <span v-else class="ep-empty">—</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="320" fixed="right" align="center" class-name="ep-action-col">
          <template #default="{ row }">
            <div class="ep-actions">
              <el-button link type="success" size="small" :icon="VideoPlay" @click="previewEpisode(row)">播放</el-button>
              <el-divider direction="vertical" />
              <el-button
                link
                type="warning"
                size="small"
                :icon="Operation"
                :loading="row._transcoding"
                :disabled="row.transcodeStatus === 'processing' || row.transcodeStatus === 'pending'"
                @click="handleTranscode(row)"
              >转码</el-button>
              <el-divider direction="vertical" />
              <el-button link type="primary" size="small" :icon="Edit" @click="openEpForm(row)">编辑</el-button>
              <el-divider direction="vertical" />
              <el-button link type="danger" size="small" :icon="Delete" @click="handleDeleteEp(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="ep-empty-state">
            <el-icon class="ep-empty-icon"><VideoPlay /></el-icon>
            <div class="ep-empty-text">暂无分集数据</div>
            <el-button type="primary" :icon="Plus" @click="openEpForm()">添加第一集</el-button>
          </div>
        </template>
      </el-table>

      <!-- 整部剧批量设置 -->
      <el-dialog title="整部剧批量设置" v-model="batchSetVisible" width="480px" append-to-body>
        <el-alert type="info" :closable="false" style="margin-bottom:16px">
          批量修改该剧所有（或指定区间）分集的解锁方式和点数价格。
        </el-alert>
        <el-form :model="batchForm" label-width="100px">
          <el-form-item label="解锁方式">
            <el-radio-group v-model="batchForm.isFree">
              <el-radio :value="1">全部免费</el-radio>
              <el-radio :value="0">付费解锁</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="batchForm.isFree === 0" label="点数价格">
            <el-input-number v-model="batchForm.price" :min="0" :precision="0" :step="10" style="width:160px" />
            <span style="margin-left:8px;color:#999;font-size:12px">
              0 = VIP专属，&gt;0 = 可用点数单集解锁
            </span>
          </el-form-item>
          <el-form-item label="应用范围">
            <el-radio-group v-model="batchForm.rangeType">
              <el-radio value="all">全部分集</el-radio>
              <el-radio value="range">指定区间</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="batchForm.rangeType === 'range'" label="集数区间">
            <el-input-number v-model="batchForm.fromEpisode" :min="1" :precision="0" style="width:120px" placeholder="从第几集" />
            <span style="margin:0 8px;color:#999">到</span>
            <el-input-number v-model="batchForm.toEpisode" :min="1" :precision="0" style="width:120px" placeholder="到第几集" />
            <span style="margin-left:8px;color:#999;font-size:12px">集</span>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="batchSetVisible = false">取消</el-button>
          <el-button type="primary" :loading="batchSubmitting" @click="submitBatchSet">确认应用</el-button>
        </template>
      </el-dialog>

      <!-- 分集表单 -->
      <el-dialog
        v-model="epFormVisible"
        width="640px"
        append-to-body
        class="ep-form-dialog"
        :show-close="false"
      >
        <template #header>
          <div class="ep-dialog-header">
            <div class="ep-dialog-title">
              <el-icon class="ep-dialog-icon"><VideoPlay /></el-icon>
              <span>{{ epForm.id ? '编辑分集' : '添加分集' }}</span>
              <el-tag v-if="epForm.episodeNum" effect="light" type="primary" round size="small" class="ep-dialog-tag">
                #{{ epForm.episodeNum }}
              </el-tag>
            </div>
            <el-icon class="ep-dialog-close" @click="epFormVisible = false"><Close /></el-icon>
          </div>
        </template>

        <el-tabs v-model="epActiveTab" class="ep-form-tabs">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-form ref="epFormRef" :model="epForm" :rules="epRules" label-width="92px" class="ep-form">
              <!-- 分组：分集信息 -->
              <div class="ep-section">
                <div class="ep-section-title"><span class="ep-bar"></span>分集信息</div>
                <el-form-item label="集数" prop="episodeNum">
                  <el-input-number v-model="epForm.episodeNum" :min="1" style="width:160px" />
                  <span class="ep-hint">影响排序与剧集编号</span>
                </el-form-item>
                <el-form-item label="默认标题" prop="title">
                  <el-input v-model="epForm.title" placeholder="如：第1集（作为多语言回退）" />
                </el-form-item>
                <el-form-item label="时长(秒)">
                  <el-input-number v-model="epForm.duration" :min="0" style="width:160px" />
                  <span class="ep-hint">{{ epForm.duration ? formatDuration(epForm.duration) : '可留空，转码后自动回填' }}</span>
                </el-form-item>
              </div>

              <!-- 分组：视频源（高亮区） -->
              <div class="ep-section ep-section--video">
                <div class="ep-section-title">
                  <span class="ep-bar"></span>视频源
                  <el-tag v-if="epForm.url" type="success" size="small" effect="light" round>已设置</el-tag>
                  <el-tag v-else type="info" size="small" effect="light" round>未设置</el-tag>
                </div>

                <el-form-item label="视频URL" prop="url">
                  <div class="ep-url-row">
                    <el-input
                      v-model="epForm.url"
                      placeholder="粘贴 OSS 地址，或通过右侧上传 / 下方下载自动填入"
                      clearable
                    />
                    <el-upload
                      action="#"
                      :show-file-list="false"
                      :before-upload="beforeVideoUpload"
                      :http-request="(opt) => handleVideoUpload(opt.file, 'url')"
                      accept="video/*"
                    >
                      <el-button type="primary" plain :icon="Upload" :loading="videoUploading.url">
                        {{ videoUploading.url ? `上传 ${videoProgress.url}%` : '上传视频' }}
                      </el-button>
                    </el-upload>
                  </div>
                </el-form-item>

                <el-form-item label="外部下载">
                  <div class="ep-url-row">
                    <el-input
                      v-model="remoteFetchUrl"
                      placeholder="填入外部视频地址（http/https）"
                      clearable
                      @keyup.enter="handleFetchRemote"
                    />
                    <el-button
                      type="warning"
                      plain
                      :icon="Download"
                      :loading="remoteFetching.url"
                      @click="handleFetchRemote"
                    >{{ remoteFetching.url ? '下载中…' : '下载到 OSS' }}</el-button>
                  </div>
                  <div class="ep-url-tip">
                    <el-icon><InfoFilled /></el-icon>
                    下载完成后会自动写入上方「视频URL」，此输入框仅用于发起下载，不参与保存
                  </div>
                </el-form-item>

                <el-form-item label="HLS地址">
                  <div class="ep-url-row">
                    <el-input
                      v-model="epForm.hlsUrl"
                      placeholder="HLS m3u8 地址（可选）"
                      clearable
                    />
                    <el-upload
                      action="#"
                      :show-file-list="false"
                      :before-upload="beforeVideoUpload"
                      :http-request="(opt) => handleVideoUpload(opt.file, 'hlsUrl')"
                      accept=".m3u8,video/*,application/vnd.apple.mpegurl"
                    >
                      <el-button plain :icon="Upload" :loading="videoUploading.hlsUrl">
                        {{ videoUploading.hlsUrl ? `上传 ${videoProgress.hlsUrl}%` : '上传文件' }}
                      </el-button>
                    </el-upload>
                  </div>
                </el-form-item>
              </div>

              <!-- 分组：计费与状态 -->
              <div class="ep-section">
                <div class="ep-section-title"><span class="ep-bar"></span>计费与状态</div>
                <el-form-item label="解锁方式">
                  <el-radio-group v-model="epForm.isFree" class="ep-segment">
                    <el-radio-button :value="1">
                      <el-icon><Unlock /></el-icon> 免费
                    </el-radio-button>
                    <el-radio-button :value="0">
                      <el-icon><Lock /></el-icon> 付费
                    </el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item v-if="epForm.isFree === 0" label="解锁价格">
                  <el-input-number v-model="epForm.price" :min="0" :precision="0" :step="10" style="width:160px" />
                  <span class="ep-hint">点数 · 0 = VIP 专属，&gt;0 = 可用点数单集解锁</span>
                </el-form-item>
                <el-form-item label="显示状态">
                  <el-radio-group v-model="epForm.status" class="ep-segment">
                    <el-radio-button :value="1">
                      <el-icon><CircleCheck /></el-icon> 显示
                    </el-radio-button>
                    <el-radio-button :value="0">
                      <el-icon><Hide /></el-icon> 隐藏
                    </el-radio-button>
                  </el-radio-group>
                </el-form-item>
              </div>
            </el-form>
          </el-tab-pane>

          <!-- 每个语言 tab -->
          <el-tab-pane
            v-for="lang in supportedLangs"
            :key="lang.code"
            :label="lang.label"
            :name="`ep_${lang.code}`"
          >
            <el-form label-width="92px" class="ep-form">
              <div class="ep-section">
                <div class="ep-section-title">
                  <span class="ep-bar"></span>{{ lang.label }} 内容
                </div>
                <el-form-item label="标题">
                  <el-input
                    v-model="epI18nForm[lang.code]"
                    :placeholder="`${lang.label}标题，如 Episode ${epForm.episodeNum || 'N'}`"
                  />
                </el-form-item>
              </div>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <template #footer>
          <div class="ep-dialog-footer">
            <el-button @click="epFormVisible = false">取消</el-button>
            <el-button type="primary" :icon="Check" @click="submitEpForm">确定保存</el-button>
          </div>
        </template>
      </el-dialog>
    </el-drawer>

  <!-- 视频播放弹窗 -->
  <el-dialog
    v-model="previewVisible"
    width="820px"
    append-to-body
    destroy-on-close
    class="preview-dialog"
    :show-close="false"
    @close="destroyPlayer"
  >
    <template #header>
      <div class="preview-header">
        <div class="preview-header-title">
          <el-icon class="preview-header-icon"><VideoPlay /></el-icon>
          <div class="preview-header-text">
            <div class="preview-title-main">
              <span>视频播放</span>
              <el-tag v-if="previewEpNum" effect="dark" type="primary" round size="small" class="preview-ep-tag">
                第 {{ previewEpNum }} 集
              </el-tag>
            </div>
            <div class="preview-title-sub" v-if="previewSubtitle">{{ previewSubtitle }}</div>
          </div>
        </div>
        <el-icon class="preview-close" @click="previewVisible = false"><Close /></el-icon>
      </div>
    </template>

    <div class="preview-wrap">
      <!-- 清晰度/格式选择 -->
      <div v-if="previewQualities.length > 1" class="quality-bar">
        <span class="quality-label">
          <el-icon><VideoCamera /></el-icon> 选择格式
        </span>
        <el-radio-group v-model="previewUrl" size="small" @change="switchQuality">
          <el-radio-button
            v-for="q in previewQualities"
            :key="q.url"
            :value="q.url"
          >{{ q.definition }}</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 播放舞台：黑底居中，自适应竖屏/横屏 -->
      <div class="preview-stage">
        <video
          ref="previewVideoRef"
          class="preview-video"
          controls
          autoplay
          playsinline
        />
      </div>

      <!-- URL 信息条 + 复制按钮 -->
      <div class="preview-url">
        <el-tag size="small" :type="previewUrlType" effect="light" round>{{ previewUrlLabel }}</el-tag>
        <span class="url-text" :title="previewUrl">{{ previewUrl }}</span>
        <el-button :icon="CopyDocument" size="small" link type="primary" @click="copyPreviewUrl">复制</el-button>
      </div>
    </div>
  </el-dialog>
</div>
</template>

<script setup>
import { listVideo, addVideo, updateVideo, deleteVideo } from '@/api/drama/video'
import { listEpisodes, addEpisode, updateEpisode, deleteEpisode, transcodeEpisode, batchSetEpisodes } from '@/api/drama/episode'
import { uploadFile, listConfig, fetchRemoteUrl } from '@/api/system/config'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Search, Refresh, VideoPlay, CircleCheck, Hide, Star,
  Setting, Clock, Loading, WarningFilled, Operation, Upload, Download, InfoFilled,
  Close, Lock, Unlock, Check, VideoCamera, CopyDocument, Film,
} from '@element-plus/icons-vue'

const headerStyle = { background: '#f7f8fa', color: '#303133', fontWeight: 600 }

function weighClass(w) {
  const v = Number(w) || 0
  if (v >= 100) return 'weigh-high'
  if (v >= 50) return 'weigh-mid'
  return 'weigh-low'
}
function relativeTime(t) {
  if (!t) return ''
  const diff = (Date.now() - new Date(t).getTime()) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前'
  if (diff < 30 * 86400) return Math.floor(diff / 86400) + ' 天前'
  if (diff < 365 * 86400) return Math.floor(diff / (30 * 86400)) + ' 个月前'
  return Math.floor(diff / (365 * 86400)) + ' 年前'
}

const LANG_LABELS = { 'zh-CN': '简体中文', 'zh-TW': '繁體中文', en: 'English' }

// ===== 视频列表 =====
const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, siteId: 1, title: '', status: '' })
const dialog = reactive({ visible: false, title: '' })
const form = ref({})
const formRef = ref()
const queryRef = ref()
const activeTab = ref('basic')
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

// 支持的语种
const supportedLangs = ref([{ code: 'zh-CN', label: '简体中文' }, { code: 'en', label: 'English' }])
const i18nForm = reactive({})

function parseJson(str) {
  try { return str ? JSON.parse(str) : {} } catch { return {} }
}

function initI18nForm(langs) {
  langs.forEach(l => { if (!i18nForm[l.code]) i18nForm[l.code] = { title: '', desc: '' } })
}

async function loadSupportedLangs() {
  try {
    const res = await listConfig()
    const configs = res.data || []
    const entry = configs.find(c => c.configKey === 'i18n.supported_langs')
    if (entry) {
      let codes = []
      try { codes = JSON.parse(entry.configValue) } catch { codes = entry.configValue.split(',').map(s => s.trim()) }
      supportedLangs.value = codes.filter(Boolean).map(code => ({ code, label: LANG_LABELS[code] || code }))
    }
  } catch { /* 使用默认值 */ }
  initI18nForm(supportedLangs.value)
}

async function getList() {
  loading.value = true
  try {
    const res = await listVideo(queryParams)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }

function handleAdd() {
  form.value = { status: 1, siteId: 1 }
  supportedLangs.value.forEach(l => { i18nForm[l.code] = { title: '', desc: '' } })
  activeTab.value = 'basic'
  dialog.title = '新增视频'
  dialog.visible = true
}

function handleEdit(row) {
  form.value = { ...row }
  const titleMap = parseJson(row.titleI18n)
  const descMap  = parseJson(row.descI18n)
  supportedLangs.value.forEach(l => {
    i18nForm[l.code] = { title: titleMap[l.code] || '', desc: descMap[l.code] || '' }
  })
  activeTab.value = 'basic'
  dialog.title = '编辑视频'
  dialog.visible = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除《${row.title}》？`, '警告', { type: 'warning' })
  await deleteVideo(row.id)
  ElMessage.success('删除成功')
  getList()
}

async function submitForm() {
  await formRef.value?.validate()
  const titleI18n = {}
  const descI18n  = {}
  supportedLangs.value.forEach(l => {
    if (i18nForm[l.code].title) titleI18n[l.code] = i18nForm[l.code].title
    if (i18nForm[l.code].desc)  descI18n[l.code]  = i18nForm[l.code].desc
  })
  const payload = {
    ...form.value,
    titleI18n: Object.keys(titleI18n).length ? JSON.stringify(titleI18n) : null,
    descI18n:  Object.keys(descI18n).length  ? JSON.stringify(descI18n)  : null,
  }
  if (payload.id) {
    await updateVideo(payload.id, payload)
  } else {
    await addVideo(payload)
  }
  ElMessage.success('操作成功')
  dialog.visible = false
  getList()
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt5M) ElMessage.error('图片不能超过 5MB')
  return isImage && isLt5M
}

async function handleUpload(file) {
  const res = await uploadFile(file)
  form.value.cover = res.data
  ElMessage.success('上传成功')
}

// ===== 分集管理 =====
const epDrawer = reactive({ visible: false, videoId: null, videoTitle: '' })
const epLoading = ref(false)
const episodes = ref([])

const epFreeCount = computed(() => episodes.value.filter(e => e.isFree).length)
const epPaidCount = computed(() => episodes.value.filter(e => !e.isFree).length)
const epTranscodedCount = computed(() => episodes.value.filter(e => e.transcodeStatus === 'done').length)

function formatDuration(sec) {
  const s = Number(sec) || 0
  if (s <= 0) return '—'
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const ss = s % 60
  if (h > 0) return `${h}:${String(m).padStart(2, '0')}:${String(ss).padStart(2, '0')}`
  return `${m}:${String(ss).padStart(2, '0')}`
}

const epFormVisible = ref(false)
const epForm = ref({})
const epFormRef = ref()
const epActiveTab = ref('basic')
const epI18nForm = reactive({})
const videoUploading = reactive({ url: false, hlsUrl: false })
const videoProgress = reactive({ url: 0, hlsUrl: 0 })
const remoteFetching = reactive({ url: false })
const remoteFetchUrl = ref('')
const epRules = {
  episodeNum: [{ required: true, message: '请输入集数', trigger: 'blur' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  url: [{ required: true, message: '请输入视频地址', trigger: 'blur' }],
}

async function openEpisodes(row) {
  epDrawer.videoId = row.id
  epDrawer.videoTitle = row.title
  epDrawer.visible = true
  await loadEpisodes()
}

async function loadEpisodes() {
  epLoading.value = true
  try {
    const res = await listEpisodes(epDrawer.videoId)
    episodes.value = res.data || []
  } finally {
    epLoading.value = false
  }
}

function openEpForm(row) {
  if (row) {
    epForm.value = { ...row }
    const titleMap = parseJson(row.titleI18n)
    supportedLangs.value.forEach(l => { epI18nForm[l.code] = titleMap[l.code] || '' })
  } else {
    const nextNum = episodes.value.length > 0
      ? Math.max(...episodes.value.map(e => e.episodeNum)) + 1
      : 1
    epForm.value = { episodeNum: nextNum, isFree: 0, price: 0, status: 1 }
    supportedLangs.value.forEach(l => { epI18nForm[l.code] = '' })
  }
  videoUploading.url = false; videoUploading.hlsUrl = false
  videoProgress.url = 0; videoProgress.hlsUrl = 0
  remoteFetching.url = false
  remoteFetchUrl.value = ''
  epActiveTab.value = 'basic'
  epFormVisible.value = true
}

function beforeVideoUpload(file) {
  const isLt2G = file.size / 1024 / 1024 / 1024 < 2
  if (!isLt2G) { ElMessage.error('单个视频文件不能超过 2GB'); return false }
  return true
}

async function handleVideoUpload(file, field) {
  videoUploading[field] = true
  videoProgress[field] = 0
  try {
    const res = await uploadFile(file, p => { videoProgress[field] = p })
    epForm.value[field] = res.data
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    videoUploading[field] = false
  }
}

async function handleFetchRemote() {
  const raw = (remoteFetchUrl.value || '').trim()
  if (!raw) { ElMessage.warning('请填写要下载的外部视频地址'); return }
  if (!/^https?:\/\//i.test(raw)) { ElMessage.warning('地址需以 http(s):// 开头'); return }
  remoteFetching.url = true
  try {
    const res = await fetchRemoteUrl(raw)
    epForm.value.url = res.data
    remoteFetchUrl.value = ''
    ElMessage.success('已下载并写入「视频URL」')
  } catch (e) {
    ElMessage.error(e?.msg || '下载失败，请检查地址是否可访问')
  } finally {
    remoteFetching.url = false
  }
}

async function submitEpForm() {
  await epFormRef.value?.validate()
  const titleI18n = {}
  supportedLangs.value.forEach(l => { if (epI18nForm[l.code]) titleI18n[l.code] = epI18nForm[l.code] })

  const payload = {
    ...epForm.value,
    url: (epForm.value.url || '').trim(),
    hlsUrl: (epForm.value.hlsUrl || '').trim(),
    titleI18n: Object.keys(titleI18n).length ? JSON.stringify(titleI18n) : null,
  }
  if (payload.id) {
    await updateEpisode(payload.id, payload)
  } else {
    await addEpisode(epDrawer.videoId, payload)
  }
  ElMessage.success('操作成功')
  epFormVisible.value = false
  loadEpisodes()
}

// ===== 整部剧批量设置 =====
const batchSetVisible = ref(false)
const batchSubmitting = ref(false)
const batchForm = reactive({ isFree: 0, price: 50, rangeType: 'all', fromEpisode: 1, toEpisode: null })

function openBatchSet() {
  batchForm.isFree = 0
  batchForm.price = 50
  batchForm.rangeType = 'all'
  batchForm.fromEpisode = 1
  batchForm.toEpisode = episodes.value.length || null
  batchSetVisible.value = true
}

async function submitBatchSet() {
  const payload = {
    isFree: batchForm.isFree,
    price: batchForm.isFree === 0 ? batchForm.price : 0,
    fromEpisode: batchForm.rangeType === 'range' ? batchForm.fromEpisode : null,
    toEpisode: batchForm.rangeType === 'range' ? batchForm.toEpisode : null,
  }
  batchSubmitting.value = true
  try {
    await batchSetEpisodes(epDrawer.videoId, payload)
    ElMessage.success('批量设置成功')
    batchSetVisible.value = false
    loadEpisodes()
  } finally {
    batchSubmitting.value = false
  }
}

async function handleDeleteEp(row) {
  await ElMessageBox.confirm(`确认删除第 ${row.episodeNum} 集《${row.title}》？`, '警告', { type: 'warning' })
  await deleteEpisode(row.id)
  ElMessage.success('删除成功')
  loadEpisodes()
}

async function handleTranscode(row) {
  if (!row.url) { ElMessage.warning('请先设置视频地址'); return }
  row._transcoding = true
  try {
    const res = await transcodeEpisode(row.id)
    if (res.code === 200) {
      ElMessage.success('转码任务已提交，后台处理中')
      row.transcodeStatus = 'pending'
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } finally {
    row._transcoding = false
  }
}

// ===== 视频播放 =====
const previewVisible = ref(false)
const previewEpNum = ref(null)
const previewSubtitle = ref('')
const previewUrl = ref('')
const previewQualities = ref([])   // [{definition, url}] 有多个时显示选择器
const previewVideoRef = ref(null)
let hlsInstance = null

const previewUrlType = computed(() => previewUrl.value.includes('.m3u8') ? 'warning' : 'success')
const previewUrlLabel = computed(() => previewUrl.value.includes('.m3u8') ? 'HLS/M3U8' : 'MP4')

function previewEpisode(row) {
  // 构建清晰度列表
  let qualities = []
  if (row.playInfo) {
    const infos = typeof row.playInfo === 'string' ? JSON.parse(row.playInfo) : row.playInfo
    if (Array.isArray(infos) && infos.length > 0) qualities = infos
  }
  // 没有 playInfo 时，把原始地址也纳入列表
  if (qualities.length === 0) {
    if (row.hlsUrl) qualities.push({ definition: 'HLS', url: row.hlsUrl })
    if (row.url)    qualities.push({ definition: 'MP4', url: row.url })
  }
  if (qualities.length === 0) { ElMessage.warning('该集暂无播放地址'); return }

  previewQualities.value = qualities
  // 默认播最高清晰度（列表末尾）
  const defaultUrl = qualities[qualities.length - 1].url
  previewEpNum.value = row.episodeNum
  previewSubtitle.value = row.title || ''
  previewUrl.value = defaultUrl
  previewVisible.value = true
  nextTick(() => initPlayer(defaultUrl))
}

async function copyPreviewUrl() {
  try {
    await navigator.clipboard.writeText(previewUrl.value || '')
    ElMessage.success('已复制视频地址')
  } catch {
    ElMessage.warning('复制失败，请手动选中地址复制')
  }
}

function switchQuality(url) {
  destroyPlayer()
  nextTick(() => initPlayer(url))
}

async function initPlayer(url) {
  const video = previewVideoRef.value
  if (!video) return

  const isHls = url.includes('.m3u8')

  if (!isHls) {
    // MP4 直接赋值
    video.src = url
    video.load()
    return
  }

  // M3U8：Safari 原生支持，其他浏览器用 HLS.js
  if (video.canPlayType('application/vnd.apple.mpegurl')) {
    video.src = url
    video.load()
    return
  }

  // 动态加载 HLS.js
  if (!window.Hls) {
    await loadScript('https://cdn.bootcdn.net/ajax/libs/hls.js/1.5.8/hls.min.js')
  }
  if (!window.Hls.isSupported()) {
    ElMessage.error('当前浏览器不支持 HLS 播放')
    return
  }
  hlsInstance = new window.Hls()
  hlsInstance.loadSource(url)
  hlsInstance.attachMedia(video)
}

function loadScript(src) {
  return new Promise((resolve, reject) => {
    const s = document.createElement('script')
    s.src = src
    s.onload = resolve
    s.onerror = reject
    document.head.appendChild(s)
  })
}

function destroyPlayer() {
  if (hlsInstance) {
    hlsInstance.destroy()
    hlsInstance = null
  }
  const video = previewVideoRef.value
  if (video) {
    video.pause()
    video.src = ''
    video.load()
  }
}

loadSupportedLangs()
getList()
</script>

<style scoped>
.video-page { padding: 12px; }

/* 工具栏 */
.toolbar {
  padding: 10px 14px 0;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.toolbar-form :deep(.el-form-item) { margin-bottom: 10px; }

/* 表格 */
.video-table {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.video-table :deep(.el-table__inner-wrapper)::before { display: none; }
.video-table :deep(.cell) { padding: 6px 10px; line-height: 1.5; }
.video-table :deep(.el-table__row) td { padding: 8px 0; }
.video-table :deep(th.el-table__cell) { padding: 8px 0; }

/* ID 徽章 */
.id-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  color: #5048e5;
  font-size: 12px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

/* 封面 */
.cover-thumb {
  width: 54px; height: 76px;
  border-radius: 6px;
  display: block;
  margin: 0 auto;
  border: 1px solid #f0f0f0;
}
.cover-thumb :deep(img) { object-fit: cover; }
.cover-empty {
  width: 54px; height: 76px;
  border-radius: 6px;
  background: #f4f6f9;
  border: 1px dashed #dcdfe6;
  color: #909399;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  user-select: none;
}

/* 短剧信息 */
.drama-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  line-height: 1.4;
}
.drama-meta { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; }
.status-tag :deep(.el-tag__content),
.status-tag {
  display: inline-flex; align-items: center; gap: 4px;
  white-space: nowrap;
}
.status-icon { font-size: 12px; }
.rec-tag :deep(.el-tag__content),
.rec-tag {
  display: inline-flex; align-items: center; gap: 4px;
  white-space: nowrap;
}
.rec-icon { font-size: 12px; }

/* 集数胶囊 */
.ep-badge {
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
  padding: 3px 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #1565c0;
}
.ep-num { font-size: 16px; font-weight: 700; font-variant-numeric: tabular-nums; }
.ep-unit { font-size: 11px; opacity: 0.7; }

/* 权重徽章 */
.weigh-badge {
  display: inline-block;
  min-width: 40px;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}
.weigh-high { background: #fff3e0; color: #e65100; }
.weigh-mid  { background: #e3f2fd; color: #1565c0; }
.weigh-low  { background: #f5f5f5; color: #606266; }

/* 时间 */
.time-cell { display: flex; flex-direction: column; gap: 0; line-height: 1.3; }
.time-abs { font-size: 12px; color: #606266; font-variant-numeric: tabular-nums; }
.time-rel { font-size: 11px; color: #909399; }

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 40px 0;
}
.empty-icon { font-size: 56px; color: #dcdfe6; }
.empty-text { color: #909399; font-size: 14px; }

/* 上传 - 弹窗内（旧版，兼容） */
.cover-uploader .cover-preview { width: 120px; height: 160px; display: block; }
.upload-placeholder {
  width: 120px; height: 160px; border: 1px dashed #d9d9d9; border-radius: 4px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; color: #8c939d; font-size: 12px; gap: 6px;
}
.upload-placeholder:hover { border-color: var(--el-color-primary); }
.upload-icon { font-size: 24px; }

/* 视频封面上传（新版，与 ep- 系列风格统一） */
.video-cover-uploader {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}
.video-cover-wrap {
  position: relative;
  width: 140px;
  height: 200px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e5e9ff;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(80, 72, 229, 0.08);
}
.video-cover-img { width: 100%; height: 100%; display: block; }
.video-cover-mask {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 4px;
  color: #fff; font-size: 12px;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.2s;
}
.video-cover-wrap:hover .video-cover-mask { opacity: 1; }
.video-cover-mask .el-icon { font-size: 20px; }
.video-cover-placeholder {
  width: 140px;
  height: 200px;
  border-radius: 10px;
  border: 1.5px dashed #c7caf5;
  background: rgba(80, 72, 229, 0.03);
  gap: 6px;
  transition: all 0.2s;
}
.video-cover-placeholder:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
  background: rgba(80, 72, 229, 0.06);
}
.video-cover-placeholder .placeholder-tip { font-size: 11px; color: #c0c4cc; }
.cover-clear-btn { padding: 0; height: auto; }

/* ============ 分集抽屉（精致风格） ============ */
.ep-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  margin-bottom: 14px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.ep-toolbar-left { display: flex; gap: 10px; align-items: center; }

.ep-stats {
  display: flex;
  align-items: center;
  gap: 18px;
}
.ep-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 4px 14px;
  border-left: 1px solid #ebeef5;
  min-width: 60px;
}
.ep-stat:first-child { border-left: none; }
.ep-stat-num {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  font-variant-numeric: tabular-nums;
  line-height: 1.1;
}
.ep-stat-label { font-size: 11px; color: #909399; }
.ep-stat--free .ep-stat-num { color: #67c23a; }
.ep-stat--paid .ep-stat-num { color: #e6a23c; }
.ep-stat--done .ep-stat-num { color: #409eff; }

/* 表格 */
.ep-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.ep-table :deep(.el-table__inner-wrapper)::before { display: none; }
.ep-table :deep(.cell) { padding: 6px 12px; line-height: 1.45; }
.ep-table :deep(th.el-table__cell) { padding: 8px 0; }
.ep-table :deep(.ep-action-col .cell) { white-space: nowrap; }

/* 操作列：4 个按钮单行展示 */
.ep-actions {
  display: inline-flex;
  align-items: center;
  flex-wrap: nowrap;
  white-space: nowrap;
  gap: 0;
}
.ep-actions .el-button + .el-button { margin-left: 0; }
.ep-actions .el-button.is-link { padding: 0 4px; }

/* 集数徽章 */
.ep-num-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  color: #5048e5;
  font-size: 12px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

/* 标题 */
.ep-title-cell { display: flex; flex-direction: column; gap: 1px; min-width: 0; }
.ep-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ep-title-sub { font-size: 11px; color: #c0c4cc; }

/* 时长 */
.ep-duration {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 12px;
  background: #f4f6f9;
  color: #606266;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}
.ep-duration .el-icon { font-size: 12px; color: #909399; }

/* 解锁胶囊 */
.ep-chip {
  display: inline-block;
  padding: 3px 14px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.ep-chip--free {
  background: linear-gradient(135deg, #e7f7e7, #c8eccb);
  color: #2e7d32;
}
.ep-chip--paid {
  background: linear-gradient(135deg, #fff7e6, #ffe7ba);
  color: #d46b08;
}

/* 状态标签 */
.ep-status-tag :deep(.el-tag__content),
.ep-status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.ep-status-icon { font-size: 12px; }

/* 转码 chip */
.ep-trans-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}
.ep-trans-chip .el-icon { font-size: 12px; }
.ep-trans-chip--done { background: #e7f7e7; color: #2e7d32; }
.ep-trans-chip--processing { background: #e8f3ff; color: #1976d2; }
.ep-trans-chip--pending { background: #f4f6f9; color: #606266; }
.ep-trans-chip--failed { background: #ffeaea; color: #c62828; cursor: help; }

/* 转码中旋转 */
.ep-spin { animation: ep-rotate 1s linear infinite; }
@keyframes ep-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 空值占位 */
.ep-empty {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 10px;
  background: #f4f6f9;
  border: 1px dashed #dcdfe6;
  color: #c0c4cc;
  font-size: 11px;
  user-select: none;
}

/* 空状态 */
.ep-empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 40px 0;
}
.ep-empty-icon { font-size: 56px; color: #dcdfe6; }
.ep-empty-text { color: #909399; font-size: 14px; }

/* 暗黑模式 */
html.dark .ep-toolbar,
html.dark .ep-table { background: #1f1f1f; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2); }
html.dark .ep-stat-num,
html.dark .ep-title { color: #e5e7eb; }
html.dark .ep-num-badge { background: rgba(80, 72, 229, 0.15); color: #a5b4fc; }
html.dark .ep-duration { background: #2a2a2a; color: #cfcfcf; }

/* 视频 URL + 上传按钮组合 */
.ep-url-row {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;
}
.ep-url-row .el-input { flex: 1; min-width: 0; }
.ep-url-row .el-upload { flex-shrink: 0; }
.ep-url-tip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
.ep-url-tip .el-icon { font-size: 13px; color: #c0c4cc; }

/* ============ 分集表单弹窗（精致风） ============ */
.ep-form-dialog :deep(.el-dialog) {
  border-radius: 14px;
  overflow: hidden;
  padding: 0;
}
.ep-form-dialog :deep(.el-dialog__header) {
  margin: 0;
  padding: 0;
  border-bottom: 1px solid #f0f0f0;
}
.ep-form-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 8px;
  background: #fafbfc;
}
.ep-form-dialog :deep(.el-dialog__footer) {
  padding: 14px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

/* 弹窗自定义头 */
.ep-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: linear-gradient(135deg, #eef2ff 0%, #f8f9ff 100%);
}
.ep-dialog-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}
.ep-dialog-icon {
  font-size: 22px;
  color: #5048e5;
  background: rgba(80, 72, 229, 0.12);
  padding: 6px;
  border-radius: 8px;
}
.ep-dialog-tag { margin-left: 4px; }
.ep-dialog-close {
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #606266;
  background: #fff;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s;
}
.ep-dialog-close:hover {
  background: #f56c6c;
  border-color: #f56c6c;
  color: #fff;
  transform: rotate(90deg);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
}

/* tabs */
.ep-form-tabs :deep(.el-tabs__header) {
  margin: 0 0 16px;
  background: #fff;
  border-radius: 10px;
  padding: 0 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.ep-form-tabs :deep(.el-tabs__nav-wrap)::after { display: none; }
.ep-form-tabs :deep(.el-tabs__item) {
  font-weight: 600;
  height: 44px;
  line-height: 44px;
}
.ep-form-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 2px;
}

/* 表单分组卡 */
.ep-form { margin-top: 0; }
.ep-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px 4px;
  margin-bottom: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
}
.ep-section--video {
  background: linear-gradient(180deg, #f8f9ff 0%, #fff 60%);
  border-color: #e5e9ff;
}
.ep-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #eee;
}
.ep-bar {
  display: inline-block;
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: linear-gradient(180deg, #5048e5 0%, #818cf8 100%);
}

/* 提示文字 */
.ep-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

/* 解锁/状态：按钮组 */
.ep-segment :deep(.el-radio-button__inner) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 18px;
  font-weight: 600;
}
.ep-segment :deep(.el-radio-button__inner .el-icon) { font-size: 13px; }
.ep-segment :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-top-left-radius: 8px;
  border-bottom-left-radius: 8px;
}
.ep-segment :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
}

/* footer 按钮 */
.ep-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 暗黑模式 */
html.dark .ep-form-dialog :deep(.el-dialog__body) { background: #1a1a1a; }
html.dark .ep-form-dialog :deep(.el-dialog__footer) { background: #1f1f1f; }
html.dark .ep-section { background: #1f1f1f; border-color: #2c2c2c; }
html.dark .ep-section--video { background: linear-gradient(180deg, #1a1d2f 0%, #1f1f1f 60%); border-color: #2c3052; }
html.dark .ep-dialog-header { background: linear-gradient(135deg, #1f1f2f 0%, #1a1a1a 100%); }
html.dark .ep-dialog-title { color: #e5e7eb; }
html.dark .ep-section-title { color: #e5e7eb; }

/* ============ 视频播放弹窗（精致风） ============ */
.preview-dialog :deep(.el-dialog) {
  border-radius: 14px;
  overflow: hidden;
  padding: 0;
  background: #fff;
}
.preview-dialog :deep(.el-dialog__header) {
  margin: 0;
  padding: 0;
  border-bottom: 1px solid #f0f0f0;
}
.preview-dialog :deep(.el-dialog__body) {
  padding: 16px 20px 20px;
  background: #fafbfc;
}

/* 自定义头部 */
.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #1e1e2f 0%, #2a2d4a 100%);
  color: #fff;
}
.preview-header-title { display: flex; align-items: center; gap: 12px; min-width: 0; flex: 1; }
.preview-header-icon {
  font-size: 22px;
  color: #fff;
  background: rgba(255, 255, 255, 0.18);
  padding: 8px;
  border-radius: 10px;
  flex-shrink: 0;
}
.preview-header-text { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.preview-title-main {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 700;
}
.preview-ep-tag { background: rgba(255, 255, 255, 0.25) !important; border-color: transparent !important; color: #fff !important; }
.preview-title-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 600px;
}
.preview-close {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #fff;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.25);
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s;
  flex-shrink: 0;
}
.preview-close:hover {
  background: #f56c6c;
  border-color: #f56c6c;
  color: #fff;
  transform: rotate(90deg);
  box-shadow: 0 2px 10px rgba(245, 108, 108, 0.45);
}

/* 内容布局 */
.preview-wrap { display: flex; flex-direction: column; gap: 12px; }

/* 清晰度切换 */
.quality-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.quality-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  white-space: nowrap;
}
.quality-label .el-icon { font-size: 14px; color: #5048e5; }

/* 播放舞台：黑色背景 + 视频居中 + 自适应竖横屏 */
.preview-stage {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
  min-height: 320px;
  max-height: 70vh;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
  position: relative;
}
.preview-stage::before {
  content: '';
  position: absolute; inset: 0;
  background: radial-gradient(ellipse at center, rgba(80, 72, 229, 0.08), transparent 60%);
  pointer-events: none;
}
.preview-video {
  max-width: 100%;
  max-height: 70vh;
  height: auto;
  display: block;
  background: #000;
  position: relative;
  z-index: 1;
}

/* URL 信息条 */
.preview-url {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.preview-url .url-text {
  flex: 1;
  font-size: 12px;
  color: #606266;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 暗黑模式 */
html.dark .preview-dialog :deep(.el-dialog__body) { background: #1a1a1a; }
html.dark .quality-bar,
html.dark .preview-url { background: #1f1f1f; }
html.dark .preview-url .url-text { color: #cfcfcf; }

/* 暗黑模式 */
html.dark .toolbar,
html.dark .video-table {
  background: #1f1f1f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
html.dark .drama-title { color: #e5e7eb; }
html.dark .id-badge { background: rgba(80, 72, 229, 0.15); color: #a5b4fc; }
html.dark .ep-badge { background: rgba(21, 101, 192, 0.15); color: #90caf9; }
</style>
