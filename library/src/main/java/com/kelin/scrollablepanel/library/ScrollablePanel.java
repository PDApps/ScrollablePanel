package com.kelin.scrollablepanel.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.HashSet;

/**
 * A flexible view for providing a limited window into a large data set,like a two-dimensional recyclerView.
 * but it will pin the itemView of first row and first column in their original location.
 */
public class ScrollablePanel extends FrameLayout {
    protected RecyclerView recyclerView;
    protected RecyclerView headerRecyclerView;
    protected PanelLineAdapter panelLineAdapter;
    protected PanelAdapter panelAdapter;
    protected FrameLayout firstItemView;
    protected RecyclerView.ViewHolder firstHeaderViewHolder;

    public ScrollablePanel(Context context, PanelAdapter panelAdapter) {
        super(context);
        this.panelAdapter = panelAdapter;
        initView();
    }

    public ScrollablePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ScrollablePanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_scrollable_panel, this, true);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_content_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        firstItemView = (FrameLayout) findViewById(R.id.first_item);
        headerRecyclerView = (RecyclerView) findViewById(R.id.recycler_header_list);
        headerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        headerRecyclerView.setHasFixedSize(true);
        if (panelAdapter != null) {
            panelLineAdapter = new PanelLineAdapter(panelAdapter, recyclerView, headerRecyclerView);
            recyclerView.setAdapter(panelLineAdapter);
            setUpFirstItemView(panelAdapter);
        }
    }

    private void setUpFirstItemView(PanelAdapter panelAdapter) {
        if (firstHeaderViewHolder == null) {
            firstHeaderViewHolder = panelAdapter.onCreateViewHolder(firstItemView, panelAdapter.getItemViewType(0, 0));
            firstItemView.addView(firstHeaderViewHolder.itemView);
        }
        panelAdapter.onBindViewHolder(firstHeaderViewHolder, 0, 0);
    }

    public void notifyDataSetChanged() {
        if (panelLineAdapter != null) {
            setUpFirstItemView(panelAdapter);
            panelLineAdapter.notifyDataChanged();
        }
    }

    /**
     * @param panelAdapter {@link PanelAdapter}
     */
    public void setPanelAdapter(PanelAdapter panelAdapter) {
        if (this.panelLineAdapter != null) {
            panelLineAdapter.setPanelAdapter(panelAdapter);
            panelLineAdapter.notifyDataSetChanged();
        } else {
            panelLineAdapter = new PanelLineAdapter(panelAdapter, recyclerView, headerRecyclerView);
            recyclerView.setAdapter(panelLineAdapter);
        }
        this.panelAdapter = panelAdapter;
        setUpFirstItemView(panelAdapter);

    }

    public class ScrollPosition implements Serializable {
        public int position;
        public int offset;
    }

    public ScrollPosition getHorizontalScrollPosition() {
        return getScrollPosition(headerRecyclerView);
    }

    public ScrollPosition getVerticalScrollPosition() {
        return getScrollPosition(recyclerView);
    }

    public ScrollPosition getScrollPosition(RecyclerView recyclerView) {
        ScrollPosition scrollPosition = new ScrollPosition();
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        scrollPosition.position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleItem = layoutManager.getChildAt(0);
        if (firstVisibleItem != null) {
            if (recyclerView == headerRecyclerView) {
                scrollPosition.offset = layoutManager.getDecoratedLeft(firstVisibleItem);
            } else {
                scrollPosition.offset = layoutManager.getDecoratedTop(firstVisibleItem);
            }
        }
        return scrollPosition;
    }

    public void scrollToHorizontal(int position, int offset) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) headerRecyclerView.getLayoutManager();
        layoutManager.scrollToPositionWithOffset(position, offset);
        for (RecyclerView recyclerView : panelLineAdapter.getRecyclerViews()) {
            layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(position, offset);
        }
        panelLineAdapter.firstPos = position;
        panelLineAdapter.firstOffset = offset;
    }

    public void scrollToVertical(int position, int offset) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        layoutManager.scrollToPositionWithOffset(position, offset);
    }

    /**
     * Adapter used to bind dataSet to cell View that are displayed within every row of {@link ScrollablePanel}.
     */
    private static class PanelLineItemAdapter extends RecyclerView.Adapter {

        private PanelAdapter panelAdapter;
        private int row;

        public PanelLineItemAdapter(int row, PanelAdapter panelAdapter) {
            this.row = row;
            this.panelAdapter = panelAdapter;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return this.panelAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            this.panelAdapter.onBindViewHolder(holder, row, position + 1);
        }

        @Override
        public int getItemViewType(int position) {
            return this.panelAdapter.getItemViewType(row, position + 1);
        }


        @Override
        public int getItemCount() {
            return panelAdapter.getColumnCount() - 1;
        }

        public void setRow(int row) {
            this.row = row;
        }
    }


    /**
     * Adapter used to bind dataSet to views that are displayed within a{@link ScrollablePanel}.
     */
    private static class PanelLineAdapter extends RecyclerView.Adapter<PanelLineAdapter.ViewHolder> {

        private PanelAdapter panelAdapter;
        private RecyclerView headerRecyclerView;
        private RecyclerView contentRV;
        private HashSet<RecyclerView> observerList = new HashSet<>();
        private int firstPos = -1;
        private int firstOffset = -1;


        public PanelLineAdapter(PanelAdapter panelAdapter, RecyclerView contentRV, RecyclerView headerRecyclerView) {
            this.panelAdapter = panelAdapter;
            this.headerRecyclerView = headerRecyclerView;
            this.contentRV = contentRV;
            initRecyclerView(headerRecyclerView);
            setUpHeaderRecyclerView();

        }

        public void setPanelAdapter(PanelAdapter panelAdapter) {
            this.panelAdapter = panelAdapter;
            setUpHeaderRecyclerView();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return panelAdapter.getRowCount() - 1;
        }

        @Override
        public PanelLineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            PanelLineAdapter.ViewHolder viewHolder = new PanelLineAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_content_row, parent, false));
            initRecyclerView(viewHolder.recyclerView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PanelLineItemAdapter lineItemAdapter = (PanelLineItemAdapter) holder.recyclerView.getAdapter();
            if (lineItemAdapter == null) {
                lineItemAdapter = new PanelLineItemAdapter(position + 1, panelAdapter);
                holder.recyclerView.setAdapter(lineItemAdapter);
            } else {
                lineItemAdapter.setRow(position + 1);
                lineItemAdapter.notifyDataSetChanged();
            }
            if (holder.firstColumnItemVH == null) {
                RecyclerView.ViewHolder viewHolder = panelAdapter.onCreateViewHolder(holder.firstColumnItemView, panelAdapter.getItemViewType(position + 1, 0));
                holder.firstColumnItemVH = viewHolder;
                panelAdapter.onBindViewHolder(holder.firstColumnItemVH, position + 1, 0);
                holder.firstColumnItemView.addView(viewHolder.itemView);
            } else {
                panelAdapter.onBindViewHolder(holder.firstColumnItemVH, position + 1, 0);
            }

            LinearLayoutManager layoutManager = (LinearLayoutManager) holder.recyclerView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(PanelLineAdapter.this.firstPos, PanelLineAdapter.this.firstOffset);
        }


        public void notifyDataChanged() {
            setUpHeaderRecyclerView();
            notifyDataSetChanged();
        }


        private void setUpHeaderRecyclerView() {
            if (panelAdapter != null) {
                if (headerRecyclerView.getAdapter() == null) {
                    PanelLineItemAdapter lineItemAdapter = new PanelLineItemAdapter(0, panelAdapter);
                    headerRecyclerView.setAdapter(lineItemAdapter);
                } else {
                    headerRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        }

        public void initRecyclerView(RecyclerView recyclerView) {
            recyclerView.setHasFixedSize(true);
            observerList.add(recyclerView);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int firstPos = linearLayoutManager.findFirstVisibleItemPosition();
                    View firstVisibleItem = linearLayoutManager.getChildAt(0);
                    int firstLeft = linearLayoutManager.getDecoratedLeft(firstVisibleItem);
                    if (dx != 0) {
                        PanelLineAdapter.this.firstPos = firstPos;
                        PanelLineAdapter.this.firstOffset = firstLeft;
                    }
                    for (RecyclerView rv : observerList) {
                        if (rv != recyclerView) {
                            final LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                            layoutManager.scrollToPositionWithOffset(PanelLineAdapter.this.firstPos, PanelLineAdapter.this.firstOffset);
                        }
                    }
                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }
            });
        }

        private HashSet<RecyclerView> getRecyclerViews() {
            HashSet<RecyclerView> recyclerViewHashSet = new HashSet<>();
            recyclerViewHashSet.add(headerRecyclerView);

            for (int i = 0; i < contentRV.getChildCount(); i++) {
                recyclerViewHashSet.add((RecyclerView) contentRV.getChildAt(i).findViewById(R.id.recycler_line_list));
            }
            return recyclerViewHashSet;
        }


        static class ViewHolder extends RecyclerView.ViewHolder {
            public RecyclerView recyclerView;
            public FrameLayout firstColumnItemView;
            public RecyclerView.ViewHolder firstColumnItemVH;

            public ViewHolder(View view) {
                super(view);
                this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_line_list);
                this.firstColumnItemView = (FrameLayout) view.findViewById(R.id.first_column_item);
                this.recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
        }

    }


}
