var idnames ={
	"强制密码历史": 1,
	"密码最长使用期限": 2,
	"密码最短使用期限": 3,
	"密码必须符合复杂性要求": 4,
	"用可还原的加密来储存密码": 5,
	"密码最小长度": 6,
	"账户锁定时间": 7,
	"账户锁定阈值": 8,
	"重置账户锁定计数器": 9,
	"审核策略更改": 10,
	"审核登录事件": 11,
	"审核对象访问": 12,
	"审核进程跟踪": 13,
	"审核目录服务访问": 14,
	"审核系统事件": 15,
	"审核账户登录事件": 16,
	"审核账户管理事件": 17,
	"作为受信任的呼叫方访问凭据管理器": 18,
	"以操作系统方式运行": 19,
	"将工作站添加到域": 20,
	"创建全局对象": 21,
	"拒绝作为批处理作业登录": 22,
	"拒绝以服务身份登录": 23,
	"拒绝本地登录": 24,
	"从远程强制关机": 25,
	"修改对象标签": 26,
	"同步目录服务数据": 27,
	"账户：来宾账户状态": 28,
	"账户：限制使用空密码的本地账户只能使用控制台登录": 29,
	"账户：重命名系统管理员账户": 30,
	"账户：重命名来宾账户": 31,
	"交互式登录：不显示上次登录用户名": 32,
	"交互式登录：无需按Ctrl+Alt+Del": 33,
	"交互式登录：计算机不活动限制": 34,
	"Microsoft网络客户端：将未加密的密码发送到第三方SMB服务器": 35,
	"Microsoft网络服务器：暂停会话前所需的空闲时间数量": 36,
	"网络安全：在下一次改变密码时不存储LAN管理器哈希值": 37,
	"网络访问：允许匿名SID/名称转换": 38,
	"网络访问：不允许SAM账户的匿名枚举": 39,
	"网络访问：不允许SAM账户和共享的匿名枚举": 40,
	"关机：允许系统在未登录前关机": 41,
	"远程桌面端口不应该为3389": 42,
	"设备自动运行": 43
}

function callback(id){
    var result = "unknow";
	for(var name in idnames){
	  if(id == idnames[name]){
	    return name;
	  }
	}
	return result;
}