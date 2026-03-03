var lastTime = new Date().getTime();
var currentTime = new Date().getTime();
var timeOut = 10 * 60 * 1000; //设置超时时间： 5分钟

var serviceUrl = 'http://' + localStorage.IP + ':' + localStorage.sort + '/PDA';
var serviceUrl = 'http://' + localStorage.IP + ':' + localStorage.sort;


$(function () {
	/* 鼠标移动事件 */
	$(document).mouseover(function () {
		lastTime = new Date().getTime(); //更新操作时间
	});
	/* 鼠标移动事件 */
	$(document).click(function () {
		lastTime = new Date().getTime(); //更新操作时间
	});
	/* 鼠标移动事件 */
	$(document).keydown(function () {
		lastTime = new Date().getTime(); //更新操作时间
	});
	/* 页面滑动事件 */
	$(document).scroll(function () {
		lastTime = new Date().getTime(); //更新操作时间
	});
});

function testTime() {
	currentTime = new Date().getTime(); //更新当前时间	
	// alert('currentTime:' + currentTime)
	// alert('lastTime:' + lastTime)
	if (currentTime - lastTime > timeOut) { //判断是否超时
		window.location.href = 'login.html'
	}
	else {
		// console.log(currentTime - lastTime);
	}
}

/* 定时器  间隔1秒检测是否长时间未操作页面  */
window.setInterval(testTime, 1000);

function getQueryVariable(variable) {
	var query = window.location.search.substring(1);
	var vars = query.split("&");
	for (var i = 0; i < vars.length; i++) {
		var pair = vars[i].split("=");
		if (pair[0] == variable) {
			return pair[1];
		}
	}
	return (false);
}
