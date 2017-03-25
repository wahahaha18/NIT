package com.aqtx.app.file;

import com.netease.nim.uikit.common.util.file.FileUtil;

import java.util.HashMap;
import java.util.Map;


public class FileIcons {

    private static final Map<String, Integer> smallIconMap = new HashMap<String, Integer>();
    static {
        smallIconMap.put("xls", com.aqtx.app.R.drawable.file_ic_session_excel);
        smallIconMap.put("ppt", com.aqtx.app.R.drawable.file_ic_session_ppt);
        smallIconMap.put("doc", com.aqtx.app.R.drawable.file_ic_session_word);
        smallIconMap.put("xlsx", com.aqtx.app.R.drawable.file_ic_session_excel);
        smallIconMap.put("pptx", com.aqtx.app.R.drawable.file_ic_session_ppt);
        smallIconMap.put("docx", com.aqtx.app.R.drawable.file_ic_session_word);
        smallIconMap.put("pdf", com.aqtx.app.R.drawable.file_ic_session_pdf);
        smallIconMap.put("html", com.aqtx.app.R.drawable.file_ic_session_html);
        smallIconMap.put("htm", com.aqtx.app.R.drawable.file_ic_session_html);
        smallIconMap.put("txt", com.aqtx.app.R.drawable.file_ic_session_txt);
        smallIconMap.put("rar", com.aqtx.app.R.drawable.file_ic_session_rar);
        smallIconMap.put("zip", com.aqtx.app.R.drawable.file_ic_session_zip);
        smallIconMap.put("7z", com.aqtx.app.R.drawable.file_ic_session_zip);
        smallIconMap.put("mp4", com.aqtx.app.R.drawable.file_ic_session_mp4);
        smallIconMap.put("mp3", com.aqtx.app.R.drawable.file_ic_session_mp3);
        smallIconMap.put("png", com.aqtx.app.R.drawable.file_ic_session_png);
        smallIconMap.put("gif", com.aqtx.app.R.drawable.file_ic_session_gif);
        smallIconMap.put("jpg", com.aqtx.app.R.drawable.file_ic_session_jpg);
        smallIconMap.put("jpeg", com.aqtx.app.R.drawable.file_ic_session_jpg);
    }

    private static final Map<String, Integer> bigIconMap = new HashMap<String, Integer>();
    static {
        bigIconMap.put("xls", com.aqtx.app.R.drawable.file_ic_detail_excel);
        bigIconMap.put("ppt", com.aqtx.app.R.drawable.file_ic_detail_ppt);
        bigIconMap.put("doc", com.aqtx.app.R.drawable.file_ic_detail_word);
        bigIconMap.put("xlsx", com.aqtx.app.R.drawable.file_ic_detail_excel);
        bigIconMap.put("pptx", com.aqtx.app.R.drawable.file_ic_detail_ppt);
        bigIconMap.put("docx", com.aqtx.app.R.drawable.file_ic_detail_word);
        bigIconMap.put("pdf", com.aqtx.app.R.drawable.file_ic_detail_pdf);
        bigIconMap.put("html", com.aqtx.app.R.drawable.file_ic_detail_html);
        bigIconMap.put("htm", com.aqtx.app.R.drawable.file_ic_detail_html);
        bigIconMap.put("txt", com.aqtx.app.R.drawable.file_ic_detail_txt);
        bigIconMap.put("rar", com.aqtx.app.R.drawable.file_ic_detail_rar);
        bigIconMap.put("zip", com.aqtx.app.R.drawable.file_ic_detail_zip);
        bigIconMap.put("7z", com.aqtx.app.R.drawable.file_ic_detail_zip);
        bigIconMap.put("mp4", com.aqtx.app.R.drawable.file_ic_detail_mp4);
        bigIconMap.put("mp3", com.aqtx.app.R.drawable.file_ic_detail_mp3);
        bigIconMap.put("png", com.aqtx.app.R.drawable.file_ic_detail_png);
        bigIconMap.put("gif", com.aqtx.app.R.drawable.file_ic_detail_gif);
        bigIconMap.put("jpg", com.aqtx.app.R.drawable.file_ic_detail_jpg);
        bigIconMap.put("jpeg", com.aqtx.app.R.drawable.file_ic_detail_jpg);
    }

    public static int smallIcon(String fileName) {
        String ext = FileUtil.getExtensionName(fileName).toLowerCase();
        Integer resId = smallIconMap.get(ext);
        if (resId == null) {
            return com.aqtx.app.R.drawable.file_ic_session_unknow;
        }

        return resId.intValue();
    }

    public static int bigIcon(String fileName) {
        String ext = FileUtil.getExtensionName(fileName).toLowerCase();
        Integer resId = bigIconMap.get(ext);
        if (resId == null) {
            return com.aqtx.app.R.drawable.file_ic_detail_unknow;
        }

        return resId.intValue();
    }
}
