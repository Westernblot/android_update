var exec = require('cordova/exec');



var myFunc = function() {};


// arg1：成功回调  
// arg2：失败回调  
// arg3：将要调用类配置的标识  
// arg4：调用的原生方法名  
// arg5：参数，数组格式  

myFunc.prototype.update = function(success, error, serverVersion, serverUrl, serverDescription) {
	exec(success, error, "WsAutoUpdate", "update", [serverVersion, serverUrl, serverDescription]);
};



var autoUpdate = new myFunc();
module.exports = autoUpdate;