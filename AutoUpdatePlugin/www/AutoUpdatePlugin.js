var exec = require('cordova/exec');



var myFunc = function() {};


// arg1���ɹ��ص�  
// arg2��ʧ�ܻص�  
// arg3����Ҫ���������õı�ʶ  
// arg4�����õ�ԭ��������  
// arg5�������������ʽ  

myFunc.prototype.update = function(success, error, serverVersion, serverUrl, serverDescription) {
	exec(success, error, "WsAutoUpdate", "update", [serverVersion, serverUrl, serverDescription]);
};



var autoUpdate = new myFunc();
module.exports = autoUpdate;