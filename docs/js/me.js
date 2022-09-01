// 全屏切换
function toggleFullScreen(e) {
    this.isFullscreenEnabled() && this.isFullScreen() && this.getFullscreenElement()
        ? this.exitFullscreen() : this.openFullscreen(e);
};

// 打开全屏
function openFullscreen(e) {
    if (e.requestFullscreen) {
        e.requestFullscreen();
    } else if (e.mozRequestFullScreen) {
        e.mozRequestFullScreen();
    } else if (e.webkitRequestFullscreen) {
        e.webkitRequestFullscreen();
    } else if (e.msRequestFullscreen) {
        e.msRequestFullscreen();
    }
}

// 退出全屏
function exitFullscreen() {
    if (document.exitFullScreen) {
        document.exitFullScreen();
    } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
    } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
    } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
    }
}

// 判断是否是全屏
function isFullScreen() {
    return !!(
        document.fullscreen ||
        document.mozFullScreen ||
        document.webkitIsFullScreen ||
        document.webkitFullScreen ||
        document.msFullScreen
    );
}

// 获取当前全屏的元素
function getFullscreenElement() {
    return (
        document.fullscreenElement ||
        document.mozFullScreenElement ||
        document.msFullScreenElement ||
        document.webkitFullscreenElement || null
    );
}

// 是否支持全屏
function isFullscreenEnabled() {
    return (
        document.fullscreenEnabled ||
        document.mozFullScreenEnabled ||
        document.webkitFullscreenEnabled ||
        document.msFullscreenEnabled
    );
}




