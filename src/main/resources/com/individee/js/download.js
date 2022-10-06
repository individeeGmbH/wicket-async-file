var individee = individee || {};
individee.initiateDownload = function (url, btnId, timeout, tooltipLastingLonger, tooltipTimeout) {
    let timer = 0;
    let timerRunning = true;
    let isDone = false;
    const btn = btnId ? $("#" + btnId) : null;
    let originalTitle;
    if (btn.attr("data-original-title")) {
        originalTitle = btn.attr("data-original-title");
    }
    try {
        console.log("Using " + url + " for download.")
        $.post({
            url: url.replace('_TYPE_', 'start'),
            dataType: 'json',
            contentType: "application/json"
        }).done(function( data ) {
            console.log("Received task-id: " + data.taskId);
            const taskId = data.taskId;
            const tid = setInterval(executeOnTimer, 2000);

            function executeOnTimer() {
                $.get({
                    url: url.replace('_TYPE_', 'status') + "&tid=" + taskId,
                    dataType: 'json',
                    contentType: "application/json"
                }).done(data => {
                    console.log("Received status. Done: " + data.done);
                    if(data.done){
                        isDone = true;
                        timerRunning = false;
                    }
                })
                if (timer === 5) {
                    if (btn && tooltipLastingLonger) {
                        console.log(tooltipLastingLonger);
                        addTooltipText(btn, tooltipLastingLonger);
                    }
                }
                if (timer * 2000 > timeout) {
                    if (btn && tooltipTimeout) {
                        console.log(tooltipTimeout);
                        addTooltipText(btn, tooltipTimeout);
                        setTimeout(() => removeTooltipText(btn, originalTitle), 3000);
                    }
                    timerRunning = false;
                }
                if (!timerRunning) {
                    if (btn) {
                        setTimeout(() => removeTooltipText(btn, originalTitle), 3000);
                    }
                    clearInterval(tid);
                    if(isDone){
                        console.log("Downloading file");
                        saveFileFromUrl(url.replace('_TYPE_', 'result') + "&tid=" + taskId)
                            .then(() => {
                                restoreTooltipText(btn);
                            })
                            .catch(err => {
                                if (err.timeout) {
                                    if (btn && tooltipTimeout) {
                                        addTooltipText(btn, tooltipTimeout);
                                        setTimeout(() => removeTooltipText(btn, originalTitle), 3000);
                                    }
                                } else {
                                    if (btn) {
                                        restoreTooltipText(btn);
                                    }
                                    console.error(err);
                                }
                                timerRunning = false;
                            });
                    } else {
                        console.warn("The task is not done");
                    }
                }
                timer++;
            }
        });

    } catch (ex) {
        console.error(ex);
        timerRunning = false;
    }

    function saveFileFromUrl(url) {
        return new Promise(function (resolve, reject) {
            const xhr = new XMLHttpRequest();
            // xhr.open(this.method, url);
            xhr.open('POST', url);
            xhr.setRequestHeader('Content-type', 'application/json');
            xhr.responseType = 'blob';
            xhr.onload = function () {
                resolve(xhr);
            };
            xhr.ontimeout = function (e) {
                reject({message: "Timed out", timeout: true});
            };
            xhr.onerror = reject;
            xhr.send();
        }).then(function (xhr) {
            const contentDisposition = xhr.getResponseHeader('Content-Disposition');
            const contentType = "application/octet-stream; charset=utf-8";
            const filename = contentDisposition.match(/filename="(.+)"/)[1];
            const file = new Blob([xhr.response], {type: contentType});
            if ('msSaveOrOpenBlob' in window.navigator) {
                window.navigator.msSaveOrOpenBlob(file, filename);
            } else {
                const data = URL.createObjectURL(file);
                const a = document.createElement("a");
                a.style = "display: none";
                a.href = data;
                a.download = decodeURI(filename);
                document.body.appendChild(a);
                a.click();
                setTimeout(function () {
                    document.body.removeChild(a);
                    window.URL.revokeObjectURL(data);
                }, 100);
            }
            return xhr;
        });
    }

    function addTooltipText(btn, tooltipText) {
        if (!btn.attr('data-ky-original-title') && btn.attr("data-original-title")) {
            btn.attr('data-ky-original-title', btn.attr('data-original-title'));
        }
        btn.attr("data-original-title", tooltipText);
    }

    function restoreTooltipText(btn) {
        if (btn.attr("data-ky-original-title")) {
            btn.attr('data-original-title', btn.attr('data-ky-original-title'));
            btn.attr('data-ky-original-title', null);
        }
    }

    function removeTooltipText(btn, originalTitle) {
        btn.removeAttr('data-ky-original-title');
        originalTitle ? btn.attr('data-original-title', originalTitle) : btn.removeAttr('data-original-title');
    }
}
