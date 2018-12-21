package com.lsjwzh.widget.powerfulscrollview;

import android.support.v7.widget.RecyclerView;

public class ScrollBlock {

  public final BlockType type;
  public final RecyclerView recyclerView;

  public ScrollBlock(RecyclerView recyclerView) {
    this.type = BlockType.RecyclerView;
    this.recyclerView = recyclerView;
  }

  public ScrollBlock() {
    this.type = BlockType.Self;
    this.recyclerView = null;
  }

  public enum BlockType {
    Self, RecyclerView
  }
}
