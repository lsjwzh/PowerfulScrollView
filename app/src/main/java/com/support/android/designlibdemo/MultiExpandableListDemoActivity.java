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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lsjwzh.widget.powerfulscrollview.PowerfulScrollView;

public class MultiExpandableListDemoActivity extends AppCompatActivity {

    PowerfulScrollView powerfulScrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_expand_list_demo);
        powerfulScrollView = findViewById(R.id.main_content);
        final RecyclerView recyclerview1 = findViewById(R.id.recyclerview1);
        RecyclerView recyclerview2 = findViewById(R.id.recyclerview2);
        setupRecyclerView(recyclerview1, "[list1]", 4);
        setupRecyclerView(recyclerview2, "[list2]", 100);
        final View tryLoadMoreView = findViewById(R.id.tryLoadMoreView);
        tryLoadMoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryLoadMoreView.setVisibility(View.GONE);
                ((SimpleStringRecyclerViewAdapter)recyclerview1.getAdapter())
                        .addValues(DemoUtils.getRandomSublist("[patch]", Cheeses.sCheeseStrings, 50));
            }
        });
        loadBackdrop();
    }

    private void setupRecyclerView(RecyclerView recyclerView, String prefix, int amount) {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(getAdapter(prefix, amount));
        powerfulScrollView.takeOverScrollBehavior(recyclerView);
    }

    @NonNull
    private SimpleStringRecyclerViewAdapter getAdapter(String prefix, int amount) {
        return new SimpleStringRecyclerViewAdapter(this,
                DemoUtils.getRandomSublist(prefix, Cheeses.sCheeseStrings, amount)) {
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
