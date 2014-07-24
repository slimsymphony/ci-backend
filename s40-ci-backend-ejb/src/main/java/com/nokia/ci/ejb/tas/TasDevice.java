/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.tas;

import java.lang.reflect.Field;

/**
 *
 * @author jajuutin
 */
public class TasDevice {

    private String ConnectionName;
    private String Connection;
    private String Imei;
    private String RmCode;
    private String HardwareType;
    private String Role;
    private String Status;
    private String StatusDetails;
    private String ReservationTime;
    private String ReservationTimeout;
    private String TasHostname;
    private String TasPort;
    private String Hostname;
    private String Ip;
    private String Port;
    private String Environment;
    private String SIM1PhoneNumber;
    private String SIM1Pin1Code;
    private String SIM1Pin2Code;
    private String SIM1Puk1Code;
    private String SIM1Puk2Code;
    private String SIM1SecurityCode;
    private String SIM1Imsi;
    private String SIM1ServiceNumber;
    private String SIM1VoiceMailNumber;
    private String SIM2PhoneNumber;
    private String SIM2Pin1Code;
    private String SIM2Pin2Code;
    private String SIM2Puk1Code;
    private String SIM2Puk2Code;
    private String SIM2SecurityCode;
    private String SIM2Imsi;
    private String SIM2ServiceNumber;
    private String SIM2VoiceMailNumber;
    // for optimization
    String content;

    @Override
    public String toString() {
        return Imei;
    }

    public String getContent() {
        if (content != null) {
            return content;
        }


        StringBuilder sb = new StringBuilder();
        for (Field f : getClass().getDeclaredFields()) {
            try {
                Object obj = f.get(this);
                if (obj != null) {
                    if (sb.length() != 0) {
                        sb.append(" ");
                    }
                    sb.append(obj.toString());
                }
            } catch (IllegalArgumentException ex) {
            } catch (IllegalAccessException ex) {
            }
        }
        content = sb.toString();
        return content;
    }

    public String getConnectionName() {
        return ConnectionName;
    }

    public void setConnectionName(String ConnectionName) {
        this.ConnectionName = ConnectionName;
        this.content = null;
    }

    public String getConnection() {
        return Connection;
    }

    public void setConnection(String Connection) {
        this.Connection = Connection;
        this.content = null;
    }

    public String getImei() {
        return Imei;
    }

    public void setImei(String Imei) {
        this.Imei = Imei;
        this.content = null;
    }

    public String getRmCode() {
        return RmCode;
    }

    public void setRmCode(String RmCode) {
        this.RmCode = RmCode;
        this.content = null;
    }

    public String getHardwareType() {
        return HardwareType;
    }

    public void setHardwareType(String HardwareType) {
        this.HardwareType = HardwareType;
        this.content = null;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String Role) {
        this.Role = Role;
        this.content = null;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
        this.content = null;
    }

    public String getStatusDetails() {
        return StatusDetails;
    }

    public void setStatusDetails(String StatusDetails) {
        this.StatusDetails = StatusDetails;
        this.content = null;
    }

    public String getReservationTime() {
        return ReservationTime;
    }

    public void setReservationTime(String ReservationTime) {
        this.ReservationTime = ReservationTime;
        this.content = null;
    }

    public String getReservationTimeout() {
        return ReservationTimeout;
    }

    public void setReservationTimeout(String ReservationTimeout) {
        this.ReservationTimeout = ReservationTimeout;
        this.content = null;
    }

    public String getTasHostname() {
        return TasHostname;
    }

    public void setTasHostname(String TasHostname) {
        this.TasHostname = TasHostname;
        this.content = null;
    }

    public String getTasPort() {
        return TasPort;
    }

    public void setTasPort(String TasPort) {
        this.TasPort = TasPort;
        this.content = null;
    }

    public String getHostname() {
        return Hostname;
    }

    public void setHostname(String Hostname) {
        this.Hostname = Hostname;
        this.content = null;
    }

    public String getIp() {
        return Ip;
    }

    public void setIp(String Ip) {
        this.Ip = Ip;
        this.content = null;
    }

    public String getPort() {
        return Port;
    }

    public void setPort(String Port) {
        this.Port = Port;
        this.content = null;
    }

    public String getEnvironment() {
        return Environment;
    }

    public void setEnvironment(String Environment) {
        this.Environment = Environment;
        this.content = null;
    }

    public String getSIM1PhoneNumber() {
        return SIM1PhoneNumber;
    }

    public void setSIM1PhoneNumber(String SIM1PhoneNumber) {
        this.SIM1PhoneNumber = SIM1PhoneNumber;
        this.content = null;
    }

    public String getSIM1Pin1Code() {
        return SIM1Pin1Code;
    }

    public void setSIM1Pin1Code(String SIM1Pin1Code) {
        this.SIM1Pin1Code = SIM1Pin1Code;
        this.content = null;
    }

    public String getSIM1Pin2Code() {
        return SIM1Pin2Code;
    }

    public void setSIM1Pin2Code(String SIM1Pin2Code) {
        this.SIM1Pin2Code = SIM1Pin2Code;
        this.content = null;
    }

    public String getSIM1Puk1Code() {
        return SIM1Puk1Code;
    }

    public void setSIM1Puk1Code(String SIM1Puk1Code) {
        this.SIM1Puk1Code = SIM1Puk1Code;
        this.content = null;
    }

    public String getSIM1Puk2Code() {
        return SIM1Puk2Code;
    }

    public void setSIM1Puk2Code(String SIM1Puk2Code) {
        this.SIM1Puk2Code = SIM1Puk2Code;
        this.content = null;
    }

    public String getSIM1SecurityCode() {
        return SIM1SecurityCode;
    }

    public void setSIM1SecurityCode(String SIM1SecurityCode) {
        this.SIM1SecurityCode = SIM1SecurityCode;
        this.content = null;
    }

    public String getSIM1Imsi() {
        return SIM1Imsi;
    }

    public void setSIM1Imsi(String SIM1Imsi) {
        this.SIM1Imsi = SIM1Imsi;
        this.content = null;
    }

    public String getSIM1ServiceNumber() {
        return SIM1ServiceNumber;
    }

    public void setSIM1ServiceNumber(String SIM1ServiceNumber) {
        this.SIM1ServiceNumber = SIM1ServiceNumber;
        this.content = null;
    }

    public String getSIM1VoiceMailNumber() {
        return SIM1VoiceMailNumber;
    }

    public void setSIM1VoiceMailNumber(String SIM1VoiceMailNumber) {
        this.SIM1VoiceMailNumber = SIM1VoiceMailNumber;
        this.content = null;
    }

    public String getSIM2PhoneNumber() {
        return SIM2PhoneNumber;
    }

    public void setSIM2PhoneNumber(String SIM2PhoneNumber) {
        this.SIM2PhoneNumber = SIM2PhoneNumber;
        this.content = null;
    }

    public String getSIM2Pin1Code() {
        return SIM2Pin1Code;
    }

    public void setSIM2Pin1Code(String SIM2Pin1Code) {
        this.SIM2Pin1Code = SIM2Pin1Code;
        this.content = null;
    }

    public String getSIM2Pin2Code() {
        return SIM2Pin2Code;
    }

    public void setSIM2Pin2Code(String SIM2Pin2Code) {
        this.SIM2Pin2Code = SIM2Pin2Code;
        this.content = null;
    }

    public String getSIM2Puk1Code() {
        return SIM2Puk1Code;
    }

    public void setSIM2Puk1Code(String SIM2Puk1Code) {
        this.SIM2Puk1Code = SIM2Puk1Code;
        this.content = null;
    }

    public String getSIM2Puk2Code() {
        return SIM2Puk2Code;
    }

    public void setSIM2Puk2Code(String SIM2Puk2Code) {
        this.SIM2Puk2Code = SIM2Puk2Code;
        this.content = null;
    }

    public String getSIM2SecurityCode() {
        return SIM2SecurityCode;
    }

    public void setSIM2SecurityCode(String SIM2SecurityCode) {
        this.SIM2SecurityCode = SIM2SecurityCode;
        this.content = null;
    }

    public String getSIM2Imsi() {
        return SIM2Imsi;
    }

    public void setSIM2Imsi(String SIM2Imsi) {
        this.SIM2Imsi = SIM2Imsi;
        this.content = null;
    }

    public String getSIM2ServiceNumber() {
        return SIM2ServiceNumber;
    }

    public void setSIM2ServiceNumber(String SIM2ServiceNumber) {
        this.SIM2ServiceNumber = SIM2ServiceNumber;
        this.content = null;
    }

    public String getSIM2VoiceMailNumber() {
        return SIM2VoiceMailNumber;
    }

    public void setSIM2VoiceMailNumber(String SIM2VoiceMailNumber) {
        this.SIM2VoiceMailNumber = SIM2VoiceMailNumber;
        this.content = null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((Imei == null) ? 0 : Imei.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TasDevice other = (TasDevice) obj;
        if (Imei == null) {
            if (other.Imei != null) {
                return false;
            }
        } else if (!Imei.equals(other.Imei)) {
            return false;
        }
        return true;
    }
}
