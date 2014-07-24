package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.model.Announcement;
import com.nokia.ci.ejb.model.AnnouncementType;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 10/3/12
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnnouncementModel {

    private Map<CssMessageType, List<String>> messages;

    public AnnouncementModel(List<? extends Announcement> announcements) {
        Map<CssMessageType, List<String>> messages = new HashMap<CssMessageType, List<String>>();
        CssMessageType[] values = CssMessageType.values();
        for (CssMessageType value : values) {
            messages.put(value, new ArrayList<String>());
        }
        for (Announcement announcement : announcements) {
            CssMessageType cssMessageType = CssMessageType.find(announcement.getType());
            messages.get(cssMessageType).add(announcement.getMessage());
        }
        this.messages=messages;
    }

    public CssMessageType[] getTypes(){
        return CssMessageType.values();
    }

    public List<String> getMessages(CssMessageType type){
        return messages.get(type);
    }

    public String getCssMessageType(CssMessageType type){
        return type.getCssClassName();
    }


    enum CssMessageType {
        INFO(AnnouncementType.INFO,"info"),
        WARN(AnnouncementType.WARNING,"warn"),
        ERROR(AnnouncementType.ERROR,"error"),
        FATAL(AnnouncementType.FATAL,"fatal");

        private AnnouncementType type;
        private String cssClassName;

        CssMessageType(AnnouncementType type,String cssClassName){
           this.type= type;
           this.cssClassName=cssClassName;
        }

        String getCssClassName(){
            return cssClassName;
        }

        static CssMessageType find(AnnouncementType type){
            CssMessageType[] enumTypes = values();
            for (CssMessageType enumType : enumTypes) {
                if (enumType.type.equals(type)){
                    return enumType;
                }
            }
            return INFO;
        }

    }


}
