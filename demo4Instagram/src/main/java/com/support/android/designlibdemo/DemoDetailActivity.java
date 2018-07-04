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

import android.os.Handler;
import android.support.v4.widget.NestedScrollViewExtend;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class DemoDetailActivity extends AppCompatActivity {

  private SimpleStringRecyclerViewAdapter adapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail_zoomable_image);
    loadBackdrop();
    setupRecyclerView((RecyclerView) findViewById(R.id.recyclerview));
  }


  private void setupRecyclerView(final RecyclerView recyclerView) {
    final InstaContainer container = (InstaContainer) findViewById(R.id.main_content);
    recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    adapter = new SimpleStringRecyclerViewAdapter(this, new ArrayList<String>()) {
      @Override
      public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.mView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            container.smoothScrollTo(0, 0);
          }
        });
      }
    };
    recyclerView.setAdapter(adapter);
    container.takeOverScrollBehavior(recyclerView);
    recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                 int oldTop, int oldRight, int oldBottom) {
        View view = findViewById(R.id.recyclerviewBottom);
        view.getLayoutParams().height
            = Math.max(0, container.getScrollableHeight() - recyclerView.getHeight());
      }
    });

    container.addOnScrollChangeListener(new NestedScrollViewExtend.OnScrollChangeListener() {
      @Override
      public void onScrollChange(NestedScrollViewExtend v, int scrollX, int scrollY,
                                 int oldScrollX, int oldScrollY) {
        Log.d("DragToZoomContainer", String.format("onScrollChange:%d,%d,%d,%d",
            scrollX, scrollY, oldScrollX, oldScrollY));
      }
    });
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        adapter.addAll(DemoUtils.getRandomSublist(Cheeses.sCheeseStrings, 20));
        adapter.notifyDataSetChanged();
//        container.takeOverScrollBehavior(recyclerView);
      }
    }, 2000);
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
