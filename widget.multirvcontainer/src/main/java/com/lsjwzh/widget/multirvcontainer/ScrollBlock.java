package com.lsjwzh.widget.multirvcontainer;

import android.support.v7.widget.RecyclerView;

public class ScrollBlock {

  public final BlockType type;
  public final RecyclerView recyclerView;

  ScrollBlock(RecyclerView recyclerView) {
    this.type = BlockType.RecyclerView;
    this.recyclerView = recyclerView;
  }

  ScrollBlock() {
    this.type = BlockType.Self;
    this.recyclerView = null;
  }

  public enum BlockType {
    Self, RecyclerView
  }
}
