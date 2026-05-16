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
    header('Content-Type: application/json');
    $video_id = $_GET['video_id'] ?? '';
    $all = read_comments();
    $list = array_filter($all, function($c) use($video_id) { return $c['video_id'] === $video_id && !$c['parent_id']; });
    foreach ($list as &$item) {
        $item['replies'] = array_values(array_filter($all, function($r) use($item) { return $r['parent_id'] === $item['id']; }));
    }
    echo json_encode(['code'=>1, 'data'=>array_values($list)], JSON_UNESCAPED_UNICODE); exit;
}
if ($action === 'add') {
    header('Content-Type: application/json');
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
        'avatar'=>$input['avatar']??'/static/img/avatar_default.png',
        'content'=>$input['content'],
        'parent_id'=>$input['parent_id']??'',
        'create_time'=>time()
    ];
    write_comments($all);
    echo json_encode(['code'=>1, 'msg'=>'评论成功']); exit;
}
if ($action === 'delete') {
    header('Content-Type: application/json');
    $input = json_decode(file_get_contents('php://input'), true);
    $raw_id = $input['id'] ?? '';
    $raw_uid = $input['user_id'] ?? '';
    if (is_array($raw_id)) $raw_id = reset($raw_id);
    if (is_array($raw_uid)) $raw_uid = reset($raw_uid);
    $del_id = strval($raw_id);
    $del_uid = strval($raw_uid);
    if (!$del_id || !$del_uid) { echo json_encode(['code'=>0, 'msg'=>'参数缺失']); exit; }
    $all = read_comments();
    $before = count($all);
    $all = array_filter($all, function($c) use($del_id, $del_uid) {
        if ((string)$c['id'] == $del_id && (string)$c['user_id'] == $del_uid) return false;
        if ((string)$c['parent_id'] == $del_id) return false;
        return true;
    });
    $after = count($all);
    file_put_contents(__DIR__.'/delete_debug.log', date('Y-m-d H:i:s')." 删除id=".var_export($input['id'],true).",uid=".var_export($input['user_id'],true).",del_id={$del_id},del_uid={$del_uid},before={$before},after={$after}\n", FILE_APPEND);
    write_comments(array_values($all));
    echo json_encode(['code'=>1, 'msg'=>'删除成功']); exit;
}
// 其它action保持不变...
// ... existing code ... 