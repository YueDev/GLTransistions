package com.example.gltransistions;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class JavaUtil {

    //获取总运存大小MB，这个数比手机无力内存小个几百MB
    public static float getTotalMemory() {

        String str1 = "/proc/meminfo";// 系统内存信息文件

        String str2;

        String[] arrayOfString;

        long initial_memory = 0;

        try {

            FileReader localFileReader = new FileReader(str1);

            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);

            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");

            for (String num : arrayOfString) {
                Log.d(str2, num + "\t");
            }

            initial_memory = Long.parseLong(arrayOfString[1]);// 获得系统总内存，单位是KB

            localBufferedReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return initial_memory / (1024f);

    }

    //请认准Qualcomm
    public static String getCpuName() {

        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            do {
                String s = br.readLine();
                if (s.contains("Hardware")) return s.split(":", 2)[1].trim();
            } while (br.read() != -1);
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    //获取cpu频率，10年前的手机主频已经到了2GHz，和现在主频相差不大。
    public static String getMaxCpuFreq() {

        StringBuilder result = new StringBuilder();

        ProcessBuilder cmd;

        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};

            cmd = new ProcessBuilder(args);

            Process process = cmd.start();

            InputStream in = process.getInputStream();

            byte[] re = new byte[24];

            while (in.read(re) != -1) {
                result.append(new String(re));
            }

            in.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            result = new StringBuilder("0");
        }
        return result.toString().trim();
    }

    //获取gpu信息， 用bin/cat的指令去dump显卡信息失败，7以上手机显卡型号和频率dump不出来

}
