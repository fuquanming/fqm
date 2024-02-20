//package com.fqm.test;
//
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.TreeSet;
//
//import org.apache.commons.collections4.list.TreeList;
//
//import com.fqm.framework.file.amazons3.filter.UploadFilter;
//
//public class UploadFilterTest {
//
//    public static void main(String[] args) {
//        TreeSet<UploadFilter> set = new TreeSet<>();
////        LinkedHashMap<Integer, UploadFilter> map = new LinkedHashMap<>();
//        
//        TreeList<UploadFilter> list = new TreeList<>();
//        
////        List<UploadFilter> list = new ArrayList<>();
//        
//        UploadFilter f1 = new UploadFilter() {
//            @Override
//            public int getOrder() {
//                return 1;
//            }
//        };
//        
//        UploadFilter f2 = new UploadFilter() {
//            @Override
//            public int getOrder() {
//                return 2;
//            }
//        };
//        
//        UploadFilter f3 = new UploadFilter() {
//            @Override
//            public int getOrder() {
//                return 3;
//            }
//        };
//        
//        UploadFilter f3_1 = new UploadFilter() {
//            @Override
//            public int getOrder() {
//                return 3;
//            }
//        };
//        
//        set.add(f2);
//        set.add(f3);
//        set.add(f3_1);
//        set.add(f1);
//        
//        System.out.println("size=" + set.size());
//        
//        set.forEach(f -> {
//            System.out.println(f.getOrder() + ":" + f.hashCode() +":" + f.toString());
//        });
//        
////        map.put(f3_1.getOrder(), f3_1);
////        map.put(f3.getOrder(), f3);
////        map.put(f2.getOrder(), f2);
////        map.put(f1.getOrder(), f1);
////        
////        map.values().forEach(f -> {
////            System.out.println(f.getOrder());
////        });
//        
////        list.add(f2);
////        list.add(f3_1);
////        list.add(f3);
////        list.add(f1);
////        
////        System.out.println("size=" + list.size());
////        
////        list.forEach(f -> {
////            System.out.println(f.getOrder());
////        });
////        
////        System.out.println("-----------");
////        
////        list.sort((o1, o2) -> {
////            if (o1.getOrder() < o2.getOrder()) {
////                return -1;
////            } else if (o1.getOrder() > o2.getOrder()) {
////                return 1;
////            } else {
////                return 0;
////            }
////        });
////        
////        list.forEach(f -> {
////            System.out.println(f.getOrder());
////        });
//        
//    }
//
//}
