MultiRecyclerViewContainer
===================================

This lib can make multi RecyclerView scroll as only one ScrollView.

Application Scene
1.Mul

Usage:
1.Add dependency.
latest-version = 0.7.1
```
dependencies {
    compile 'com.github.lsjwzh.MultiRVScrollView:widget.multirvcontainer:latest-version'
}
```
2.Layout
```
<com.lsjwzh.widget.multirvcontainer.MultiRVScrollView xmlns:android="http://schemas.android.com/apk/res/android"
                                                      android:id="@+id/main_content"
                                                      android:layout_width="match_parent"
                                                      android:layout_height="match_parent"
                                                      android:fitsSystemWindows="true">

  <LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <ImageView
      android:id="@+id/backdrop"
      android:layout_width="match_parent"
      android:layout_height="@dimen/detail_backdrop_height"
      android:scaleType="centerCrop"/>

    <android.support.v7.widget.RecyclerView
      android:id="@+id/recyclerview1"
      android:overScrollMode="never"
      android:layout_width="match_parent"
      android:layout_height="match_parent">
    </android.support.v7.widget.RecyclerView>

    <com.lsjwzh.widget.multirvcontainer.CoordinateScrollRecyclerView
      android:id="@+id/recyclerview2"
      android:overScrollMode="never"
      android:layout_width="match_parent"
      android:layout_height="match_parent">
    </com.lsjwzh.widget.multirvcontainer.CoordinateScrollRecyclerView>

  </LinearLayout>
</com.lsjwzh.widget.multirvcontainer.MultiRVScrollView>
```

CoordinateScrollRecyclerView is not necessary while you have not call 'scrollToPosition' manually.

3.Java Code

    mMultiRVScrollView = (MultiRVScrollView) findViewById(R.id.main_content);
    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
    mRecyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(this,
        DemoUtils.getRandomSublist(Cheeses.sCheeseStrings, 200)));
    // this must be called to support MultiRecyclerView in one ScrollView.
    mMultiRVScrollView.takeOverScrollBehavior(mRecyclerView);

4.Remove MultiRecyclerView support

    mMultiRVScrollView.handOverScrollBehavior(mRecyclerView);


License
-------

Copyright 2017 lsjwzh.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
