/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.support.android.designlibdemo;

import android.os.Bundle;
import com.lsjwzh.widget.InstaContainer;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class DemoDetailActivity extends AppCompatActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail_zoomable_image);
    loadBackdrop();
    setupRecyclerView((RecyclerView) findViewById(R.id.recyclerview));
  }


  private void setupRecyclerView(final RecyclerView recyclerView) {
    recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(this,
        DemoUtils.getRandomSublist(Cheeses.sCheeseStrings, 20)));
    final InstaContainer container = (InstaContainer) findViewById(R.id.main_content);
    container.takeOverScrollBehavior(recyclerView);
    recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                 int oldTop, int oldRight, int oldBottom) {
        View view = findViewById(R.id.recyclerviewBottom);
        view.getLayoutParams().height = container.getScrollableHeight() - recyclerView.getHeight();
      }
    });

    container.addOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
      @Override
      public void onScrollChange(NestedScrollView v, int scrollX, int scrollY,
                                 int oldScrollX, int oldScrollY) {
        Log.d("DragToZoomContainer", String.format("onScrollChange:%d,%d,%d,%d",
            scrollX, scrollY, oldScrollX, oldScrollY));
      }
    });

  }


  private void loadBackdrop() {
    final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
    Glide.with(this).load(Cheeses.getRandomCheeseDrawable()).centerCrop().into(imageView);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.sample_actions, menu);
    return true;
  }
}
