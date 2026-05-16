<?php
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET,POST,OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') exit;

$DATA_FILE = __DIR__ . '/comments.json';
function read_comments() {
    global $DATA_FILE;
    if (!file_exists($DATA_FILE)) return [];
    $json = file_get_contents($DATA_FILE);
    return json_decode($json, true) ?: [];
}
function write_comments($data) {
    global $DATA_FILE;
    file_put_contents($DATA_FILE, json_encode($data, JSON_UNESCAPED_UNICODE|JSON_PRETTY_PRINT));
}

$action = $_GET['action'] ?? '';
if ($action === 'list') {
    $video_id = $_GET['video_id'] ?? '';
    $all = read_comments();
    $list = array_filter($all, function($c) use($video_id) { return $c['video_id'] === $video_id && !$c['parent_id']; });
    foreach ($list as &$item) {
        $item['replies'] = array_values(array_filter($all, function($r) use($item) { return $r['parent_id'] === $item['id']; }));
    }
    echo json_encode(['code'=>1, 'data'=>array_values($list)], JSON_UNESCAPED_UNICODE);
    exit;
}
if ($action === 'add') {
    $input = json_decode(file_get_contents('php://input'), true);
    if (!$input['video_id'] || !$input['user_id'] || !$input['content']) {
        echo json_encode(['code'=>0, 'msg'=>'参数缺失']); exit;
    }
    $all = read_comments();
    $id = time() . rand(1000,9999);
    $all[] = [
        'id'=>$id,
        'video_id'=>$input['video_id'],
        'user_id'=>$input['user_id'],
        'user_nick'=>$input['user_nick']??'',
        'content'=>$input['content'],
        'parent_id'=>$input['parent_id']??'',
        'create_time'=>time()
    ];
    write_comments($all);
    echo json_encode(['code'=>1, 'msg'=>'评论成功']);
    exit;
}
if ($action === 'delete') {
    $input = json_decode(file_get_contents('php://input'), true);
    if (!$input['id'] || !$input['user_id']) { echo json_encode(['code'=>0, 'msg'=>'参数缺失']); exit; }
    $all = read_comments();
    $all = array_filter($all, function($c) use($input) { return !($c['id']===$input['id'] && $c['user_id']===$input['user_id']) && $c['parent_id']!==$input['id']; });
    write_comments(array_values($all));
    echo json_encode(['code'=>1, 'msg'=>'删除成功']);
    exit;
}
// 管理页面
if ($action === 'admin') {
?><!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<title>评论管理后台</title>
<style>body{font-family:Arial;margin:20px;}table{border-collapse:collapse;width:100%;}th,td{border:1px solid #ccc;padding:8px;}th{background:#f5f5f5;}input,textarea{width:100%;}.actions button{margin-right:5px;}</style>
</head>
<body>
<h2>评论管理后台</h2>
<button onclick="loadComments()">刷新</button>
<button onclick="showForm()">新增评论</button>
<table id="commentTable"><thead><tr><th>ID</th><th>视频ID</th><th>用户ID</th><th>昵称</th><th>内容</th><th>父评论</th><th>时间</th><th>操作</th></tr></thead><tbody></tbody></table>
<div id="formDiv" style="display:none;margin-top:20px;"><h3 id="formTitle">新增/编辑评论</h3><form onsubmit="return saveComment()"><input type="hidden" id="id"><label>视频ID: <input id="video_id" required></label><br><label>用户ID: <input id="user_id" required></label><br><label>昵称: <input id="user_nick"></label><br><label>内容: <textarea id="content" required></textarea></label><br><label>父评论ID: <input id="parent_id"></label><br><label>时间戳: <input id="create_time"></label><br><button type="submit">保存</button><button type="button" onclick="hideForm()">取消</button></form></div>
<script>
function loadComments(){fetch('?action=all').then(r=>r.json()).then(data=>{const tbody=document.querySelector('#commentTable tbody');tbody.innerHTML='';data.forEach(c=>{const tr=document.createElement('tr');tr.innerHTML=`<td>${c.id}</td><td>${c.video_id}</td><td>${c.user_id}</td><td>${c.user_nick||''}</td><td>${c.content}</td><td>${c.parent_id||''}</td><td>${c.create_time}</td><td class="actions"><button onclick='editComment(${JSON.stringify(c)})'>编辑</button><button onclick='deleteComment("${c.id}","${c.user_id}")'>删除</button></td>`;tbody.appendChild(tr);});});}
function showForm(c={}){document.getElementById('formDiv').style.display='';document.getElementById('formTitle').innerText=c.id?'编辑评论':'新增评论';document.getElementById('id').value=c.id||'';document.getElementById('video_id').value=c.video_id||'';document.getElementById('user_id').value=c.user_id||'';document.getElementById('user_nick').value=c.user_nick||'';document.getElementById('content').value=c.content||'';document.getElementById('parent_id').value=c.parent_id||'';document.getElementById('create_time').value=c.create_time||'';}
function hideForm(){document.getElementById('formDiv').style.display='none';}
function saveComment(){const c={id:document.getElementById('id').value,video_id:document.getElementById('video_id').value,user_id:document.getElementById('user_id').value,user_nick:document.getElementById('user_nick').value,content:document.getElementById('content').value,parent_id:document.getElementById('parent_id').value,create_time:document.getElementById('create_time').value};fetch('?action=save',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(c)}).then(r=>r.json()).then(res=>{if(res.code===1){alert('保存成功');hideForm();loadComments();}else{alert(res.msg||'保存失败');}});return false;}
function editComment(c){showForm(c);}
function deleteComment(id,uid){if(!confirm('确定删除？'))return;fetch('?action=delete',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({id:id,user_id:uid})}).then(r=>r.json()).then(res=>{if(res.code===1){alert('删除成功');loadComments();}else{alert(res.msg||'删除失败');}});}
loadComments();
</script>
</body></html>
<?php exit; }
if ($action === 'all') {
    echo json_encode(read_comments(), JSON_UNESCAPED_UNICODE); exit;
}
if ($action === 'save') {
    $c = json_decode(file_get_contents('php://input'), true);
    if (!$c['video_id'] || !$c['user_id'] || !$c['content']) { echo json_encode(['code'=>0, 'msg'=>'参数缺失']); exit; }
    $all = read_comments();
    if ($c['id']) {
        $idx = array_search($c['id'], array_column($all, 'id'));
        if ($idx !== false) {
            $all[$idx] = array_merge($all[$idx], $c);
        } else {
            $all[] = $c;
        }
    } else {
        $c['id'] = time() . rand(1000,9999);
        $c['create_time'] = $c['create_time'] ?: time();
        $all[] = $c;
    }
    write_comments($all);
    echo json_encode(['code'=>1, 'msg'=>'保存成功']); exit;
}
// 默认首页
?><!DOCTYPE html>
<html lang="zh-CN"><head><meta charset="UTF-8"><title>PHP评论后端</title></head><body><h2>PHP评论后端已运行</h2><ul><li><a href="?action=admin">管理后台</a></li><li>API示例：<ul><li>GET ?action=list&video_id=xxx</li><li>POST ?action=add</li><li>POST ?action=delete</li></ul></li></ul></body></html> 