const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const fs = require('fs');
const path = require('path');

const app = express();
app.use(cors());
app.use(bodyParser.json());

const DATA_FILE = path.join(__dirname, 'comments.json');

// 读取评论数据
function readComments() {
  if (!fs.existsSync(DATA_FILE)) return [];
  try {
    return JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
  } catch (e) {
    return [];
  }
}

// 写入评论数据
function writeComments(data) {
  fs.writeFileSync(DATA_FILE, JSON.stringify(data, null, 2), 'utf8');
}

// ========== 前台API ==========
// 获取评论列表
app.get('/api/commentList', (req, res) => {
  const { video_id } = req.query;
  let all = readComments();
  let list = all.filter(c => c.video_id === video_id && (!c.parent_id));
  for (let item of list) {
    item.replies = all.filter(r => r.parent_id === item.id);
  }
  res.json({ code: 1, data: list });
});

// 添加评论
app.post('/api/addComment', (req, res) => {
  const { video_id, user_id, user_nick, content, parent_id } = req.body;
  if (!video_id || !user_id || !content) return res.json({ code: 0, msg: '参数缺失' });
  let all = readComments();
  const id = Date.now().toString() + Math.floor(Math.random() * 10000);
  all.push({
    id,
    video_id,
    user_id,
    user_nick,
    content,
    parent_id: parent_id || '',
    create_time: Math.floor(Date.now() / 1000),
    avatar: req.body.avatar || '/static/img/avatar_default.png'
  });
  writeComments(all);
  res.json({ code: 1, msg: '评论成功' });
});

// 删除评论
app.post('/api/deleteComment', (req, res) => {
  const { id, user_id } = req.body;
  if (!id || !user_id) return res.json({ code: 0, msg: '参数缺失' });
  let all = readComments();
  all = all.filter(c => !(c.id === id && c.user_id === user_id) && c.parent_id !== id);
  writeComments(all);
  res.json({ code: 1, msg: '删除成功' });
});

// ========== 可视化管理后台 ==========
// 获取全部评论
app.get('/admin/all', (req, res) => {
  res.json(readComments());
});

// 新增/编辑评论
app.post('/admin/save', (req, res) => {
  let all = readComments();
  const c = req.body;
  if (!c.video_id || !c.user_id || !c.content) return res.json({ code: 0, msg: '参数缺失' });
  if (c.id) {
    // 编辑
    let idx = all.findIndex(x => x.id === c.id);
    if (idx > -1) {
      all[idx] = { ...all[idx], ...c };
    } else {
      all.push({ ...c, id: c.id });
    }
  } else {
    // 新增
    c.id = Date.now().toString() + Math.floor(Math.random() * 10000);
    c.create_time = c.create_time || Math.floor(Date.now() / 1000);
    c.avatar = c.avatar || '/static/img/avatar_default.png';
    all.push(c);
  }
  writeComments(all);
  res.json({ code: 1, msg: '保存成功' });
});

// 删除评论
app.post('/admin/delete', (req, res) => {
  let all = readComments();
  const { id } = req.body;
  if (!id) return res.json({ code: 0, msg: '参数缺失' });
  all = all.filter(c => c.id !== id && c.parent_id !== id);
  writeComments(all);
  res.json({ code: 1, msg: '删除成功' });
});

// 管理页面
app.get('/admin', (req, res) => {
  res.sendFile(path.join(__dirname, 'admin.html'));
});

const port = process.env.PORT || 3000;
app.listen(port, () => {
  console.log('评论后端已启动：http://localhost:' + port);
}); 