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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lsjwzh.widget.powerfulscrollview.PowerfulScrollView;

public class EmbedRecyclerViewDemoActivity extends AppCompatActivity {
  public static final String MODE = "mode";

  PowerfulScrollView mMultiRVScrollView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String mode = getIntent().getStringExtra(MODE);
    if (mode.equals(MainActivity.BANNER_LIST)) {
      setContentView(R.layout.banner_list_demo);
      mMultiRVScrollView = findViewById(R.id.main_content);
      setupRecyclerView(R.id.recyclerview2, "[list1]");
    } else if (mode.equals(MainActivity.LIST_LIST)) {
      setContentView(R.layout.list_list_demo);
      mMultiRVScrollView = findViewById(R.id.main_content);
      setupRecyclerView(R.id.recyclerview, "[list1]");
      setupRecyclerView(R.id.recyclerview2, "[list2]");
    } else if (mode.equals(MainActivity.LIST_BANNER_LIST)) {
      setContentView(R.layout.list_banner_list_demo);
      mMultiRVScrollView = findViewById(R.id.main_content);
      setupRecyclerView(R.id.recyclerview, "[list1]");
      setupRecyclerView(R.id.recyclerview2, "[list2]");
    }
    loadBackdrop();
  }

  private void setupRecyclerView(int id, String prefix) {
    RecyclerView recyclerView = findViewById(id);
    recyclerView.setLayoutManager(new LinearLayoutManager(this){
      @Override
      public boolean isAutoMeasureEnabled() {
        return true;
      }
    });
    recyclerView.setAdapter(getAdapter(prefix));
    mMultiRVScrollView.takeOverScrollBehavior(recyclerView);
  }

  @NonNull
  private SimpleStringRecyclerViewAdapter getAdapter(String prefix) {
    return new SimpleStringRecyclerViewAdapter(this,
        DemoUtils.getRandomSublist(prefix, Cheeses.sCheeseStrings, 5)) {
      @Override
      public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Toast.makeText(v.getContext(), "test", Toast.LENGTH_SHORT).show();
          }
        });
      }
    };
  }

  private void loadBackdrop() {
    final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
    if (imageView == null) {
      return;
    }
    Glide.with(this).load(Cheeses.getRandomCheeseDrawable()).centerCrop().into(imageView);
    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.sample_actions, menu);
    return true;
  }
}
