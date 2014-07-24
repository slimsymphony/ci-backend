/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hhellgre
 */
public class TimezoneUtil {
    
    private static final Map<String, String> timezones;
    static {
        Map<String, String> tzMap = new HashMap<String, String>();
        tzMap.put("us", "America/New_York");
        tzMap.put("us1", "America/Chicago");
        tzMap.put("us2", "America/Denver");
        tzMap.put("us3", "America/Los_Angeles");
        tzMap.put("us4", "America/Anchorage");
        tzMap.put("us5", "America/Halifax");

        tzMap.put("ca", "Canada/Pacific");
        tzMap.put("ca1", "Canada/Mountain");
        tzMap.put("ca2", "Canada/Central");
        tzMap.put("ca3", "Canada/Eastern");
        tzMap.put("ca4", "Canada/Atlantic");

        tzMap.put("au", "Australia/Sydney");
        tzMap.put("au1", "Australia/Darwin");
        tzMap.put("au2", "Australia/Perth");

        tzMap.put("ru", "Europe/Moscow");
        tzMap.put("ru1", "Europe/Samara");
        tzMap.put("ru2", "Asia/Yekaterinburg");
        tzMap.put("ru3", "Asia/Novosibirsk");
        tzMap.put("ru4", "Asia/Krasnoyarsk");
        tzMap.put("ru5", "Asia/Irkutsk");
        tzMap.put("ru6", "Asia/Chita");
        tzMap.put("ru7", "Asia/Vladivostok");

        tzMap.put("an", "Europe/Andorra");
        tzMap.put("ae", "Asia/Abu_Dhabi");
        tzMap.put("af", "Asia/Kabul");
        tzMap.put("al", "Europe/Tirana");
        tzMap.put("am", "Asia/Yerevan");
        tzMap.put("ao", "Africa/Luanda");
        tzMap.put("ar", "America/Buenos_Aires");
        tzMap.put("as", "Pacific/Samoa");
        tzMap.put("at", "Europe/Vienna");
        tzMap.put("aw", "America/Aruba");
        tzMap.put("az", "Asia/Baku");

        tzMap.put("ba", "Europe/Sarajevo");
        tzMap.put("bb", "America/Barbados");
        tzMap.put("bd", "Asia/Dhaka");
        tzMap.put("be", "Europe/Brussels");
        tzMap.put("bf", "Africa/Ouagadougou");
        tzMap.put("bg", "Europe/Sofia");
        tzMap.put("bh", "Asia/Bahrain");
        tzMap.put("bi", "Africa/Bujumbura");
        tzMap.put("bm", "Atlantic/Bermuda");
        tzMap.put("bn", "Asia/Brunei");
        tzMap.put("bo", "America/La_Paz");
        tzMap.put("br", "America/Sao_Paulo");
        tzMap.put("bs", "America/Nassau");
        tzMap.put("bw", "Gaborone");
        tzMap.put("by", "Europe/Minsk");
        tzMap.put("bz", "America/Belize");

        tzMap.put("cd", "Africa/Kinshasa");
        tzMap.put("ch", "Europe/Zurich");
        tzMap.put("ci", "Africa/Abidjan");
        tzMap.put("cl", "America/Santiago");
        tzMap.put("cn", "Asia/Shanghai");
        tzMap.put("co", "America/Bogota");
        tzMap.put("cr", "America/Costa_Rica");
        tzMap.put("cu", "America/Cuba");
        tzMap.put("cv", "Atlantic/Cape_Verde");
        tzMap.put("cy", "Asia/Nicosia");
        tzMap.put("cz", "Europe/Prague");

        tzMap.put("de", "Europe/Berlin");
        tzMap.put("dj", "Africa/Djibouti");
        tzMap.put("dk", "Europe/Copenhagen");
        tzMap.put("do", "America/Santo_Domingo");
        tzMap.put("dz", "Africa/Algiers");

        tzMap.put("ec", "America/Quito");
        tzMap.put("ee", "Europe/Tallinn");
        tzMap.put("eg", "Africa/Cairo");
        tzMap.put("er", "Africa/Asmara");
        tzMap.put("es", "Europe/Madrid");

        tzMap.put("fi", "Europe/Helsinki");
        tzMap.put("fj", "Pacific/Fiji");
        tzMap.put("fk", "America/Stanley");
        tzMap.put("fr", "Europe/Paris");
        
        tzMap.put("ga", "Africa/Libreville");
        tzMap.put("gb", "Europe/London");
        tzMap.put("gd", "America/Grenada");
        tzMap.put("ge", "Asia/Tbilisi");
        tzMap.put("gh", "Africa/Accra");
        tzMap.put("gm", "Africa/Banjul");
        tzMap.put("gn", "Africa/Conakry");
        tzMap.put("gr", "Europe/Athens");
        tzMap.put("gy", "America/Guyana");
        
        tzMap.put("hk", "Asia/Hong_Kong");
        tzMap.put("hn", "America/Tegucigalpa");
        tzMap.put("hr", "Europe/Zagreb");
        tzMap.put("ht", "America/Port-au-Prince");
        tzMap.put("hu", "Europe/Budapest");

        tzMap.put("id", "Asia/Jakarta");
        tzMap.put("ie", "Europe/Dublin");
        tzMap.put("il", "Asia/Tel_Aviv");
        tzMap.put("in", "Asia/Calcutta");
        tzMap.put("iq", "Asia/Baghdad");
        tzMap.put("ir", "Asia/Tehran");
        tzMap.put("is", "Atlantic/Reykjavik");
        tzMap.put("it", "Europe/Rome");

        tzMap.put("jm", "America/Jamaica");
        tzMap.put("jo", "Asia/Amman");
        tzMap.put("jp", "Asia/Tokyo");

        tzMap.put("ke", "Africa/Nairobi");
        tzMap.put("kg", "Asia/Bishkek");
        tzMap.put("kh", "Asia/Phnom_Penh");
        tzMap.put("kp", "Asia/Pyongyang");
        tzMap.put("kr", "Asia/Seoul");
        tzMap.put("kw", "Asia/Kuwait");

        tzMap.put("lb", "Asia/Beirut");
        tzMap.put("li", "Europe/Liechtenstein");
        tzMap.put("lk", "Asia/Colombo");
        tzMap.put("lr", "Africa/Monrovia");
        tzMap.put("ls", "Africa/Maseru");
        tzMap.put("lt", "Europe/Vilnius");
        tzMap.put("lu", "Europe/Luxembourg");
        tzMap.put("lv", "Europe/Riga");
        tzMap.put("ly", "Africa/Tripoli");

        tzMap.put("ma", "Africa/Rabat");
        tzMap.put("mc", "Europe/Monaco");
        tzMap.put("md", "Europe/Chisinau");
        tzMap.put("mg", "Indian/Antananarivo");
        tzMap.put("mk", "Europe/Skopje");
        tzMap.put("ml", "Africa/Bamako");
        tzMap.put("mm", "Asia/Rangoon");
        tzMap.put("mn", "Asia/Ulaanbaatar");
        tzMap.put("mo", "Asia/Macao");
        tzMap.put("mq", "America/Martinique");
        tzMap.put("mt", "Europe/Malta");
        tzMap.put("mu", "Indian/Mauritius");
        tzMap.put("mv", "Indian/Maldives");
        tzMap.put("mw", "Africa/Lilongwe");
        tzMap.put("mx", "America/Mexico_City");
        tzMap.put("my", "Asia/Kuala_Lumpur");

        tzMap.put("na", "Africa/Windhoek");
        tzMap.put("ne", "Africa/Niamey");
        tzMap.put("ng", "Africa/Lagos");
        tzMap.put("ni", "America/Managua");
        tzMap.put("nl", "Europe/Amsterdam");
        tzMap.put("no", "Europe/Oslo");
        tzMap.put("np", "Asia/Kathmandu");
        tzMap.put("nz", "Pacific/Aukland");

        tzMap.put("om", "Asia/Muscat");

        tzMap.put("pa", "America/Panama");
        tzMap.put("pe", "America/Lima");
        tzMap.put("pg", "Pacific/Port_Moresby");
        tzMap.put("ph", "Asia/Manila");
        tzMap.put("pk", "Asia/Karachi");
        tzMap.put("pl", "Europe/Warsaw");
        tzMap.put("pr", "America/Puerto_Rico");
        tzMap.put("pt", "Europe/Lisbon");
        tzMap.put("py", "America/Asuncion");

        tzMap.put("qa", "Asia/Qatar");

        tzMap.put("ro", "Europe/Bucharest");
        tzMap.put("rs", "Europe/Belgrade");

        tzMap.put("rw", "Africa/Kigali");

        tzMap.put("sa", "Asia/Riyadh");
        tzMap.put("sd", "Africa/Khartoum");
        tzMap.put("se", "Europe/Stockholm");
        tzMap.put("sg", "Asia/Singapore");
        tzMap.put("si", "Europe/Ljubljana");
        tzMap.put("sk", "Europe/Bratislava");
        tzMap.put("sl", "Africa/Freetown");
        tzMap.put("so", "Africa/Mogadishu");
        tzMap.put("sr", "America/Paramaribo");
        tzMap.put("sv", "America/El_Salvador");
        tzMap.put("sy", "Asia/Damascus");
        tzMap.put("sz", "Africa/Mbabane");
       
        tzMap.put("td", "Africa/Ndjamena");
        tzMap.put("tg", "Africa/Lome");
        tzMap.put("th", "Asia/Bangkok");
        tzMap.put("tj", "Asia/Dushanbe");
        tzMap.put("tm", "Asia/Ashgabat");
        tzMap.put("tn", "Africa/Tunis");
        tzMap.put("to", "Pacific/Tongatapu");
        tzMap.put("tr", "Asia/Istanbul");
        tzMap.put("tw", "Asia/Taipei");
        tzMap.put("tz", "Africa/Dar_es_Salaam");

        tzMap.put("ua", "Europe/Kiev");
        tzMap.put("ug", "Africa/Kampala");
        tzMap.put("uk", "Europe/London");
        tzMap.put("uy", "America/Montevideo");
        tzMap.put("uz", "Asia/Tashkent");

        tzMap.put("ve", "America/Caracas");
        tzMap.put("vn", "Asia/Hanoi");

        tzMap.put("za", "Africa/Johannesburg");
        tzMap.put("zm", "Africa/Lusaka");
        tzMap.put("zw", "Africa/Harare");
        
        timezones = Collections.unmodifiableMap(tzMap);
    }
    
    public static String getTimezoneByCountryId(String countryId)
    {
        if(countryId == null) {
            return "Europe/Helsinki";
        }
        
        String country = countryId.toLowerCase();
        
        if(timezones.containsKey(country))
        {
            return timezones.get(country);
        }
        
        return "Europe/Helsinki";
    }
    
    public static String getTimezoneByNokiaSite(String nokiaSite)
    {
        if(nokiaSite == null || nokiaSite.length() < 2) {
            return "Europe/Helsinki";
        }
        
        return getTimezoneByCountryId(nokiaSite.substring(0, 2));
    }
    
    public static String[] getTimeZones()
    {
        return timezones.values().toArray(new String[0]);
    }
    
    public static List<String> getTimezoneList() {
        List<String> ret =  new ArrayList<String>(timezones.values());
        Collections.sort(ret);
        return ret;
    }
}
