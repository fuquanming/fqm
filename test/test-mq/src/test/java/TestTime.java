//import java.util.HashMap;
//import java.util.Map;
//import java.util.TreeMap;
//import java.util.concurrent.TimeUnit;
//
//import com.fqm.framework.mq.util.RocketDelayUtil;
//import com.fqm.test.mq.model.User;
//
//public class TestTime {
//
//    public static void main(String[] args) {
//        // 延迟时间存在十八个等级 (1s/5s/10s/30s/1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m/1h/2h )
//        
//        TimeUnit tu = TimeUnit.SECONDS;
//        int time = 5;
//        long out = tu.toSeconds(time);
//        System.out.println(tu.toSeconds(time));
//        System.out.println(tu.toMinutes(time));
//        System.out.println(tu.toHours(time));
//        
//        Map<Integer, Integer> secondMap = new HashMap<>();
//        for (int i = 0; i < 60; i++) {
//            if (i <= 1) {
//                secondMap.put(i, 1);
//            } else if (i > 1 && i < 5) {
//                secondMap.put(i, 1);
//            } else if (i >= 5 && i < 10) {
//                secondMap.put(i, 5);
//            } else if (i >= 10 && i < 30) {
//                secondMap.put(i, 10);
//            } else if (i >= 30 && i < 60) {
//                secondMap.put(i, 30);
//            }
//        }
//        System.out.println(secondMap);
//        System.out.println(secondMap.get((int)tu.toSeconds(1)));
//        System.out.println(secondMap.get((int)tu.toSeconds(2)));
//        System.out.println(secondMap.get((int)tu.toSeconds(5)));
//        System.out.println(secondMap.get((int)tu.toSeconds(7)));
//        System.out.println(secondMap.get((int)tu.toSeconds(10)));
//        System.out.println(secondMap.get((int)tu.toSeconds(12)));
//        System.out.println(secondMap.get((int)tu.toSeconds(30)));
//        System.out.println(secondMap.get((int)tu.toSeconds(50)));
//        System.out.println(secondMap.get((int)tu.toSeconds(59)));
//        System.out.println(secondMap.get((int)tu.toSeconds(60)));
//        
//        System.out.println("-------------");
//        // 1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m
//        Map<Integer, Integer> minuteMap = new HashMap<>();
//        for (int i = 0; i < 60; i++) {
//            if (i <= 1) {
//                minuteMap.put(i, 1);
//            } else if (i > 1 && i < 2) {
//                minuteMap.put(i, 1);
//            } else if (i >= 2 && i < 3) {
//                minuteMap.put(i, 2);
//            } else if (i >= 3 && i < 4) {
//                minuteMap.put(i, 3);
//            } else if (i >= 4 && i < 5) {
//                minuteMap.put(i, 4);
//            } else if (i >= 5 && i < 6) {
//                minuteMap.put(i, 5);
//            } else if (i >= 6 && i < 7) {
//                minuteMap.put(i, 6);
//            } else if (i >= 7 && i < 8) {
//                minuteMap.put(i, 7);
//            } else if (i >= 8 && i < 9) {
//                minuteMap.put(i, 8);
//            } else if (i >= 9 && i < 10) {
//                minuteMap.put(i, 9);
//            } else if (i >= 10 && i < 20) {
//                minuteMap.put(i, 10);
//            } else if (i >= 20 && i < 30) {
//                minuteMap.put(i, 20);
//            } else if (i >= 30 && i < 60) {
//                minuteMap.put(i, 30);
//            }
//        }
//        System.out.println(minuteMap);
////        tu = TimeUnit.MINUTES;
//        // 1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m
////        System.out.println(minuteMap.get((int)tu.toMinutes(1)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(2)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(3)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(4)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(5)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(7)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(8)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(10)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(12)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(30)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(50)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(60)));
//        System.out.println(minuteMap.get((int)tu.toMinutes(60)));
//        System.out.println(minuteMap.get((int)tu.toMinutes(61)));
//        System.out.println(minuteMap.get((int)tu.toMinutes(119)));
//        System.out.println(minuteMap.get((int)tu.toMinutes(120)));
//        System.out.println(minuteMap.get((int)tu.toMinutes(121)));
//        System.out.println(minuteMap.get((int)tu.toMinutes(156)));
//        System.out.println(minuteMap.get((int)tu.toMinutes(180)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(10)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(12)));
////        System.out.println(minuteMap.get((int)tu.toMinutes(30)));
//        System.out.println(minuteMap.get((int)tu.toMinutes(60 * 30)));
//        System.out.println(minuteMap.get((int)tu.toMinutes(60 * 59)));
//        System.out.println(minuteMap.get((int)tu.toMinutes(60 * 60)));
//        
//        System.out.println("-----------");
//        Map<Integer, Integer> hourMap = new TreeMap<>();
//        for (int i = 0; i < 3; i++) {
//            if (i <= 1) {
//                hourMap.put(i, 1);
//            } else if (i > 1 && i < 2) {
//                hourMap.put(i, 1);
//            } else if (i >= 2) {
//                hourMap.put(i, 2);
//            }
//        }
//        System.out.println(hourMap);
////        tu = TimeUnit.HOURS;
//        // 1h/2h
//        System.out.println(hourMap.get((int)tu.toHours(3600)));
//        System.out.println(hourMap.get((int)tu.toHours(3600 + 60)));
//        System.out.println(hourMap.get((int)tu.toHours(7200)));
//        System.out.println(hourMap.get((int)tu.toHours(7200 + 1)));
//        System.out.println(hourMap.get((int)tu.toHours(7200 + 3599)));
//        System.out.println(hourMap.get((int)tu.toHours(7200 + 3600)));
//        Object[] name = hourMap.values().toArray();
//        System.out.println("--" + name[name.length - 1]);
//        
//        System.out.println("---------");
//        
//        System.out.println(RocketDelayUtil.getDelayLevel(2, TimeUnit.SECONDS));
//        System.out.println(RocketDelayUtil.getDelayLevel(5, TimeUnit.SECONDS));
//        System.out.println(RocketDelayUtil.getDelayLevel(6, TimeUnit.SECONDS));
//        System.out.println(RocketDelayUtil.getDelayLevel(10, TimeUnit.SECONDS));
//        System.out.println(RocketDelayUtil.getDelayLevel(19, TimeUnit.SECONDS));
//        System.out.println(RocketDelayUtil.getDelayLevel(30, TimeUnit.SECONDS));
//        System.out.println(RocketDelayUtil.getDelayLevel(31, TimeUnit.SECONDS));
//        System.out.println(RocketDelayUtil.getDelayLevel(60, TimeUnit.SECONDS));
//        System.out.println(RocketDelayUtil.getDelayLevel(61, TimeUnit.SECONDS));
//        System.out.println(RocketDelayUtil.getDelayLevel(2, TimeUnit.HOURS));
//        System.out.println(RocketDelayUtil.getDelayLevel(3, TimeUnit.HOURS));
//        System.out.println("---");
//        
//        User user = new User();
//        user.setId(1L);
//        System.out.println(RocketDelayUtil.buildDelayMsg(user, 5, TimeUnit.SECONDS));
//        System.out.println(TimeUnit.MILLISECONDS.toDays(Integer.MAX_VALUE));
//        
//    }
//    
//}
