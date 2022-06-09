function HolliSecSAS(param){
    var result = "";
    var siddhiparam={};
    if(param["sd_type"] == "1"){
        var patt = /12[8-9].0.0.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/;
        if(!patt.test(param["sd_src_ip"]) || !patt.test(param["sd_dst_ip"])){
            if(param["sd_protocol"] == "TCP"){
                siddhiparam={"streamname":"outer_access","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                             "tag":"tcp outer communication", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":"HOLY-2"}
                result = "input,"+JSON.stringify(param);
            }else if(param["sd_protocol"] == "smb" || param["sd_protocol"] == "ftp"){
                siddhiparam={"streamname":"outer_access","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                            "tag":"smb ftp outer communication", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":"HOLY-3"}
                result = "input,"+JSON.stringify(param);
            }
        }
    }if(param["sd_type"] == "3"){
        if(param["sd_operate"] == "update FCS"){
            siddhiparam={"streamname":"cri_operation","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                           "tag":"unauthorized update", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":"HOLY-8"}
            result = "input,"+JSON.stringify(siddhiparam);
        }else if(param["sd_operate"] == "download HMI"){
            siddhiparam={"streamname":"cri_operation","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                         "tag":"unauthorized download", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":"HOLY-9"}
            result = "input,"+JSON.stringify(param);
        }else if(param["sd_operate"] == "43"){
            if(param["protocol"] =="ModbusTCP"){
                siddhiparam={"streamname":"cri_operation","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                             "tag":"device detect protocol", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":"HOLY-10"}
                result = "input,"+JSON.stringify(param);
            }
        }
    }else if(param["sd_type"] == "4"){
        siddhiparam={"streamname":"impair_process","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                   "tag":"unauthorized dev ip detect", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":"HOLY-4"}
        result = "input,"+JSON.stringify(siddhiparam);
    }else if(param["sd_type"] == "7"){
        siddhiparam={"streamname":"impair_process","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                    "tag":param["sd_baseline"], "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":"HOLY-5"}
        result = "input,"+JSON.stringify(siddhiparam);
    }else if(param["sd_type"] == "9"){
        siddhiparam={"streamname":"impair_process","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                     "tag":"password cracking", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":"HOLY-9"}
        result = "input,"+JSON.stringify(siddhiparam);
    }
    return result;
}

function NsfocusSas(param){
    var result = "";
    var siddhiparam={};
    var patt = /12[8-9].0.0.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/;
    if(!patt.test(param["sd_src_ip"]) || !patt.test(param["sd_dst_ip"])){
        if(param["sd_protocol"] == "OPC_DA"){
            if(param["sd_role"] != 'remote'){
                siddhiparam={"streamname":"outer_access","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                             "tag":"IOC warning", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
                result += "input,"+JSON.stringify(siddhiparam);
            }
        }else{
            siddhiparam={"streamname":"outer_access","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                         "tag":"other outer communication", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
            result += "input,"+JSON.stringify(siddhiparam);
        }
    }else if(patt.test(param["sd_src_ip"]) && !patt.test(param["sd_dst_ip"])){
        siddhiparam={"streamname":"outer_access","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                     "tag":"in_to_out connection", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
        result += "input,"+JSON.stringify(siddhiparam);
    }

    if( (param["sd_protocol"] == "MACS6" || param["sd_protocol"] == "macs6") &&
        (param["sd_operate"] == "reboot" || param["sd_operate"] == "stop" || param["sd_operate"] == "config" || param["sd_operate"] == "update")
      ){
        siddhiparam={"streamname":"cri_operation","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
           "tag":"critical operation", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
         result += "input,"+JSON.stringify(siddhiparam);
    }else if(param["sd_src_ip"] == param["sd_dst_ip"] && param["sd_role"] == "FCS"){
        siddhiparam={"streamname":"impair_process","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                   "tag":"FCS TO FCS warning", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
        result += "input,"+JSON.stringify(siddhiparam);
    }else if(param["sd_protocol"] == "TCP" && (param["sd_role"] == "OPS" || param["sd_role"] == "SVR")){
        siddhiparam={"streamname":"impair_process","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                   "tag":"ops and svr rule warning", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
        result += "input,"+JSON.stringify(siddhiparam);
    }else if(param["sd_protocol"] == "TCP" && (param["sd_role"] == "ENG" || param["sd_role"] == "FCS")){
        siddhiparam={"streamname":"impair_process","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                    "tag":"eng and fcs rule warning", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
        result += "input,"+JSON.stringify(siddhiparam);
    }else if(param["sd_protocol"] == "Modbus_TCP" || param["sd_protocol"] == "S7" || param["sd_protocol"] == "EthernetIP"){
        siddhiparam={"streamname":"impair_process","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                     "tag":"other industrial protocol", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
        result += "input,"+JSON.stringify(siddhiparam);
    }else if(param["sd_protocol"] == "HTTP" || param["sd_protocol"] == "DNS"){
        siddhiparam={"streamname":"impair_process","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                     "tag":"other protocol", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
        result += "input,"+JSON.stringify(siddhiparam);
    }else if(param["sd_protocol"] == "unlegal"){
        siddhiparam={"streamname":"impair_process","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                     "tag":"illegal protocol format", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
        result += "input,"+JSON.stringify(siddhiparam);
    }else if(param["sd_protocol"] == "RDP" || param["sd_protocol"] == "SSH" || param["sd_protocol"] == "SMB" || param["sd_protocol"] == "Telnet"){
        siddhiparam={"streamname":"lateral_movement","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                     "tag":"remote login", "level":"Medium", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
        result += "input,"+JSON.stringify(siddhiparam);
    }else if(param["sd_protocol"] == "FTP" || param["sd_protocol"] == "SFTP"){
        siddhiparam={"streamname":"lateral_movement","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                     "tag":"file trasmition", "level":"Medium", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
        result += "input,"+JSON.stringify(siddhiparam);
    }

    if(param["sd_operate"] == "scanning"){
        siddhiparam={"streamname":"scanning","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                     "tag":"scanning", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
        result += "input,"+JSON.stringify(siddhiparam);
    }else if(param["sd_operate"] == "password cracking"){
        siddhiparam={"streamname":"exploit","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                     "tag":"password cracking", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
        result += "input,"+JSON.stringify(siddhiparam);
    }
    return result;
}

function NsfocusIds(param){
    var result = "";
    var siddhiparam={};
    siddhiparam={"streamname":"exploit","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_operate"], "protocol":param["sd_protocol"],
                 "tag":"IDS", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
    result += "input,"+JSON.stringify(siddhiparam);
    return result;
}

function Firewall(param){
    var result = "";
    var siddhiparam={};
    siddhiparam={"streamname":"exploit","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"], "dst_ip":param["sd_dst_ip"], "operate":param["sd_warning"], "protocol":param["sd_protocol"],
                 "tag":"FW", "level":"High", "uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
    result += "input,"+JSON.stringify(siddhiparam);
    return result;
}

function WindowsSecurity(param){
    var result = "";
    var siddhiparam={};
    if(param["sd_event_id"] == "4720" || param["sd_event_id"] == "4728"){
        siddhiparam={"streamname":"lateral_movement","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"],"dst_ip":"","operate":"add user","protocol":param["sd_detail"],
           "tag":"add user","level":"Medium","uuid":param["sd_uuid"], "src_uuid":"", "rulenum":"HOLY-37"}
        result += "input,"+JSON.stringify(siddhiparam);
    }
    return result;
}

function Agent(param){
    var result = "";
    var siddhiparam={};
    if(param["sd_event_id"] == "tcpudpPorts"){
         siddhiparam={"streamname":"command_control","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"],"dst_ip":"","info":"critical opened ports","detail":param["sd_detail"],
           "tag":"critical opened ports","level":"High","uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
         result += "agent,"+JSON.stringify(siddhiparam);
    }else if(param["sd_event_id"] == "USB"){
         siddhiparam={"streamname":"udisk_access","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"],"dst_ip":"","info":"udisk insert","detail":param["sd_detail"],
           "tag":"udisk insert","level":"Medium","uuid":param["sd_uuid"], "src_uuid":"", "rulenum":"HOLY-23"}
         result += "agent,"+JSON.stringify(siddhiparam);
    }else if(param["sd_event_id"] == "start proecess"){
         var detail = param["sd_detail"];
         if(detail.indexOf("\\") > 0){
             detail = detail.replace(/\\/g,"\\\\");
         }
         siddhiparam={"streamname":"command_control","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"],"dst_ip":"","info":"unknown process","detail":detail,
                    "tag":"unknown process","level":"High","uuid":param["sd_uuid"], "src_uuid":"", "rulenum":"HOLY-31"}
         result += "agent,"+JSON.stringify(siddhiparam);
    }/*else if(param["sd_event_id"] == "ipmac"){
              siddhiparam={"streamname":"command_control","timestamp":param["timestamp"],"src_ip":param["sd_src_ip"],"dst_ip":"","info":"IP and MAC collision","detail":param["sd_detail"],
                "tag":"IP and MAC collision","level":"Low","uuid":param["sd_uuid"], "src_uuid":"", "rulenum":""}
              result += "agent,"+JSON.stringify(siddhiparam);
         }*/
    return result;
}

function callback(str){
    var result = "";
    if(str.length <= 0){
       return "";
    }
    var  param = JSON.parse(str);
    if(param["sd_event_source"] == "HOLLiSec-SAS"){
        result = HolliSecSAS(param);
    }else if(param["sd_event_source"] == "SAS-ICS"){ //nsfocus sas
        result = NsfocusSas(param);
    }else if(param["sd_event_source"] == "degree"){//nsfocus ids
        result = NsfocusIds(param);
    }else if(param["sd_event_source"] == "firewall"){
        result = Firewall(param);
    }else if(param["sd_event_source"] == "Microsoft-Windows-Security-Auditing"){
        result = WindowsSecurity(param);
    }else if(param["sd_event_source"] == "agent"){
        result = Agent(param);
    }
    return result;
}