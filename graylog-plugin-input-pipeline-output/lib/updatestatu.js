function callback(str){
    var key = "snmp-status:{";
    var begin = str.search(key);
    var end = str.search("}");

    if(begin > 0 && end > 0){
        var res = str.substring(begin+key.length-1,end+1);
        var devstatus = JSON.parse(res);
        //ram
        if(devstatus.ramUsageRate == -1) {
            if(devstatus.ramIdle > 0 && devstatus.ramUsage > 0){
                devstatus.ramUsageRate = Math.round(devstatus.ramUsage*100 / (devstatus.ramIdle + devstatus.ramUsage));
            }else if(devstatus.ramIdle > 0 && devstatus.ramSize > 0){
                devstatus.ramUsageRate = Math.round((devstatus.ramSize - devstatus.ramIdle)*100 / devstatus.ramSize);
            }else if(devstatus.ramSize > 0 && devstatus.ramUsage > 0){
                devstatus.ramUsageRate = Math.round(devstatus.ramUsage*100 / devstatus.ramSize);
            }
        }
        //disk
        if(devstatus.diskIdle == -1){
            if(devstatus.diskSize > 0 && devstatus.diskUsage > 0){
                devstatus.diskIdle = devstatus.diskSize - devstatus.diskUsage;
            }
        }
        return JSON.stringify(devstatus);
    }

    return "";
}