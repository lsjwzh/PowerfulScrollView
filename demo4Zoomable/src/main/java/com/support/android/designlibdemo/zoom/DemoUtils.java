package com.support.android.designlibdemo.zoom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by wenye on 2017/9/21.
 */
public class DemoUtils {

  public static List<String> getRandomSublist(String[] array, int amount) {
    ArrayList<String> list = new ArrayList<>(amount);
    Random random = new Random();
    while (list.size() < amount) {
      list.add(array[random.nextInt(array.length)]);
    }
    return list;
  }
}
