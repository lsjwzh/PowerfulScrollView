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

package com.support.android.designlibdemo.zoom;

import android.os.Bundle;
import com.lsjwzh.widget.PullToZoomContainer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ZoomableImageDetailActivity extends AppCompatActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail_zoomable_image);

    final PullToZoomContainer refreshContainer
        = (PullToZoomContainer) findViewById(R.id.main_content);
    RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerview);
    refreshContainer.takeOverScrollBehavior(rv);
    setupRecyclerView(rv);
    loadBackdrop();
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

  private void setupRecyclerView(RecyclerView recyclerView) {
    recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(this,
        DemoUtils.getRandomSublist(Cheeses.sCheeseStrings, 30)) {
      @Override
      public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.itemView.setOnClickListener(null);
      }
    });
  }
}
