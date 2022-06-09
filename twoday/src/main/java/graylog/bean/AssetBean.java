package graylog.bean;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AssetBean {
    private String ip;

    private String assetType;

    @JsonProperty("AssetStat")
    private AssetStatBean AssetStat;

}
