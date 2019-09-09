package com.kelin.scrollablepanel.library;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VerticalLineAdapter extends RecyclerView.Adapter<VerticalLineAdapter.LineViewHolder> {

    private PanelAdapter panelAdapter;
    private ArrayList<DottedLine> dottedLines = new ArrayList<>();

    VerticalLineAdapter(PanelAdapter panelAdapter) {
        this.panelAdapter = panelAdapter;
    }

    @NonNull
    @Override
    public LineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DottedLine view = new DottedLine(parent.getContext());
        dottedLines.add(view);
        return new LineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LineViewHolder holder, int position) {
        DottedLine dottedLine = (DottedLine) holder.itemView;
        dottedLine.setLayoutParams(new FrameLayout.LayoutParams(panelAdapter.getWidthOfColumn(), FrameLayout.LayoutParams.MATCH_PARENT));
        boolean isDotted = panelAdapter.isEmptyColumn(position + 1);
        int color = panelAdapter.getColumnColor(position + 1);
        dottedLine.setDotted(isDotted);
        dottedLine.setColor(color);
    }

    @Override
    public int getItemCount() {
        return panelAdapter.getColumnCount() - 1;
    }

    static class LineViewHolder extends RecyclerView.ViewHolder {

        public LineViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
