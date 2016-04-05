<!DOCTYPE HTML>
<html>
<body onload="start()">
    ASR: <span id="asr"></span>
     
    <script type="text/javascript">
    function start() {
 
        var eventSource = new EventSource("diatree");
         
            eventSource.onmessage = function(event) {
         
            document.getElementById('asr').innerHTML = event.data;
         
        };
         
    }
    </script>
</body>
</html>