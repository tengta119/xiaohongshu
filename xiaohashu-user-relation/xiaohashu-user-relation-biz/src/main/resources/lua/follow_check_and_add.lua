---@diagnostic disable: undefined-global
-- LUA 脚本：校验并添加关注关系

-- 操作的 Redis Key
local key = KEYS[1]
-- 关注的用户 ID
local followUserId = ARGV[1]
-- 时间戳
local timestamp = ARGV[2]

-- 使用 EXISTS 命令检查 ZSET 是否存在
local expire = redis.call('EXISTS', key);
if expire == 0 then
    return -1
end

-- 校验关注人数是否上限（是否达到 1000）
local size = redis.call("ZCARD", key)
if size >= 1000 then
    return -2
end

-- 校验目标用户是否已经关注
if redis.call("ZSCORE", key, followUserId) then
    return -3
end

-- ZADD 添加关注关系
redis.call("ZADD", key, timestamp, followUserId)
return 0







