package sonata.kernel.adaptor.commons;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QosObject {

  private Integer latency;

  @JsonProperty("latency_unit")
  private String latencyUnit;

  private Integer bandwidth;

  @JsonProperty("bandwidth_unit")
  private String bandwidthUnit;


  public Integer getLatency() {
    return latency;
  }

  public String getLatencyUnit() {
    return latencyUnit;
  }

  public Integer getBandwidth() {
    return bandwidth;
  }

  public String getBandwidthUnit() {
    return bandwidthUnit;
  }


  public void setLatency(Integer latency) {
    this.latency = latency;
  }

  public void setLatencyUnit(String latencyUnit) {
    this.latencyUnit = latencyUnit;
  }

  public void setBandwidth(Integer bandwidth) {
    this.bandwidth = bandwidth;
  }

  public void setBandwidthUnit(String bandwidthUnit) {
    this.bandwidthUnit = bandwidthUnit;
  }
}
