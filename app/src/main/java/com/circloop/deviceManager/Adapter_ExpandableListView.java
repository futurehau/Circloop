package com.circloop.deviceManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.circloop.database.Child;
import com.circloop.database.Group;

public class Adapter_ExpandableListView extends BaseExpandableListAdapter {
    private String TAG = "Adapter_ExpandableListView";
    private Context mContext;
    private LayoutInflater mInflater = null;
    private List<List<Child>> mData = null;
    private List<Group> groupList;
    private List<ViewGroup_SlideDelete> slideDeleteArrayList = new ArrayList<ViewGroup_SlideDelete>(); // 记录侧滑界面是否显示
    private List<ViewGroup_SlideDelete> slideDeleteArrayList_forChild = new ArrayList<ViewGroup_SlideDelete>();
    private OnEditGroupClickListener onEditGroupClickListener;
    private static InetAddress address;//为了检测设备是否在线
    private static ExecutorService LIMIT_TASK_EXECUTOR = (ExecutorService) Executors.newFixedThreadPool(50);//可以限定线程池的大小

    public static boolean isVisible_batch_delete = false;//批量删除分组复选框是否可见
    public static boolean isVisible_batch_delete_child = false; //批量删除孩子复选框是否可见

    private MainActivity mainActivity;
    private Fragment_list fragment_list;
    private HashMap<Group,Boolean> isCheckedMap;
    private int checkedItemNum;
    private int getCheckedItemNum_child;
    private int currentGroupIndex; // 当前操作批量删除的分组
    private HashMap<Child,Boolean> isCheckedMap_child;
    public Adapter_ExpandableListView(MainActivity mainActivity,Fragment_list fragment_list,Context ctx, List<Group> groupList, List<List<Child>> list) {
        mContext = ctx;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = list;
        this.groupList = groupList;
        this.mainActivity = mainActivity;
        this.fragment_list = fragment_list;
    }

    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return mData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        return mData.get(groupPosition).size();
    }

    @Override
    public List<Child> getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return mData.get(groupPosition);
    }

    @Override
    public Child getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return mData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded,
                             View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        GroupViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.group_item_layout, null);
            holder = new GroupViewHolder();
            holder.mGroupName = (TextView) convertView
                    .findViewById(R.id.group_name);
            holder.mGroupCount = (TextView) convertView
                    .findViewById(R.id.group_count);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            holder.progressText = (TextView) convertView.findViewById(R.id.progressText);
            holder.mElvContent = (LinearLayout) convertView.findViewById(R.id.mElvContent);
            holder.mEdit = (LinearLayout) convertView.findViewById(R.id.edit_llayout);
            holder.mDelete = (LinearLayout) convertView.findViewById(R.id.del_llayout);
            holder.mSlideDelete = (ViewGroup_SlideDelete) convertView.findViewById(R.id.mSlideDelete);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.group_view_for_date_checkbox);
            convertView.setTag(R.id.mElvContent, groupPosition);
            convertView.setTag(R.id.child_item, -1);
            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        holder.mGroupName.setText(groupList.get(groupPosition).getGroupName());
        holder.mGroupCount.setText(" [" + mData.get(groupPosition).size() + "]");

        // 初始化CheckBox的选中状态
        initialCheckBoxForGroup(holder, groupPosition);

        // group_item点击监听
        final GroupViewHolder finalHolder = holder;
        holder.mElvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupItemClicked(groupPosition, finalHolder, isExpanded);
            }
        });

        // group_item长按监听
        holder.mElvContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return groupItemLongClicked();
            }
        });

        // checkBox点击监听
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupCheckBoxClicked(groupPosition);
            }
        });

        // 编辑分组监听
        holder.mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditGroupClickListener.onEditGroupClick(groupPosition);
                //点击编辑分组之后，Fragment_list中的onEditGroupClick方法启动 Edit_Group Activiry进行分组编辑，onActivityResult方法接收编辑的结果
                closeOtherItem();//编辑之后返回列表界面不显示编辑删除按钮
            }
        });

        // 删除分组监听
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(groupPosition);
            }
        });

        //侧滑分组监听
        holder.mSlideDelete.setOnSlideDeleteListener(new ViewGroup_SlideDelete.OnSlideDeleteListener() {//接口回调，为了避免同时出现多个item的删除视图的问题
            @Override
            public void onOpen(ViewGroup_SlideDelete slideDelete) {
                collapseGroup();
                closeOtherItem();
                closeOtherItemForChild();
                slideDeleteArrayList.add(slideDelete);
            }

            @Override
            public void onClose(ViewGroup_SlideDelete slideDelete) {
                slideDeleteArrayList.remove(slideDelete);
            }
        });

        groupShowState(groupPosition, holder);
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ChildViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.child_item_layout, null);
            holder = new ChildViewHolder();
            holder.mIcon = (ImageView) convertView.findViewById(R.id.img);
            holder.mChildName = (TextView) convertView.findViewById(R.id.item_name);
            holder.mDetail = (TextView) convertView.findViewById(R.id.item_detail);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.child_view_for_date_checkbox);
            holder.mElvContent_child = (LinearLayout) convertView.findViewById(R.id.mElvContent_Child);
            holder.mSlideDelete = (ViewGroup_SlideDelete) convertView.findViewById(R.id.mSlideDelete_child);
            holder.mDelete = (LinearLayout) convertView.findViewById(R.id.del_layout_child);
            holder.mEdit = (LinearLayout) convertView.findViewById(R.id.edit_layout_child);
            convertView.setTag(holder);
            convertView.setTag(R.id.mElvContent, groupPosition);
            convertView.setTag(R.id.child_item, childPosition);
        }
        else {
            holder = (ChildViewHolder) convertView.getTag();
        }
        holder.mIcon.setBackgroundResource(R.drawable.b);
        holder.mChildName.setText(getChild(groupPosition, childPosition).getDeviceType());
        String ip = getChild(groupPosition, childPosition).getIp();
        holder.mDetail.setText(ip);

        if (getChild(groupPosition, childPosition).getOnline()) {//设备在线     注意在代码中设置字体颜色必须使用color 用dralable无效
            holder.mChildName.setTextColor(mContext.getResources().getColor(R.color.textColorBlack));
            holder.mDetail.setTextColor(mContext.getResources().getColor(R.color.textColorBlack));
        } else {//设备离线
            holder.mChildName.setTextColor(mContext.getResources().getColor(R.color.textColorGrey));
            holder.mDetail.setTextColor(mContext.getResources().getColor(R.color.textColorGrey));
        }

        // 初始化CheckBox的选中状态
        initialCheckBoxForChild(groupPosition, childPosition, holder);

        // checkBox点击监听
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                childCheckBoxClicked(groupPosition, childPosition);
            }
        });

        // child Item点击监听
        final ChildViewHolder finalHolder = holder;
        holder.mElvContent_child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                childItemClicked(groupPosition, childPosition, finalHolder);
            }
        });

        //child Item长按监听
        holder.mElvContent_child.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return childItemLongClicked(groupPosition);
            }
        });

        // 编辑孩子监听
        holder.mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "你点击了编辑设备按钮", Toast.LENGTH_SHORT).show();
                closeOtherItemForChild();
            }
        });

        // 删除孩子监听
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem_child(groupPosition, childPosition);
            }
        });

        //侧滑孩子监听
        holder.mSlideDelete.setOnSlideDeleteListener(new ViewGroup_SlideDelete.OnSlideDeleteListener() {//接口回调，为了避免同时出现多个item的删除视图的问题
            @Override
            public void onOpen(ViewGroup_SlideDelete slideDelete) {
                closeOtherItemForChild();
                closeOtherItem();
                slideDeleteArrayList_forChild.add(slideDelete);
            }

            @Override
            public void onClose(ViewGroup_SlideDelete slideDelete) {
                slideDeleteArrayList_forChild.remove(slideDelete);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        // TODO Auto-generated method stub
        /*
         * ChildView 设置 布局很可能onChildClick进不来，要在 ChildView layout 里加上
         * android:descendantFocusability="blocksDescendants",
         * 还有isChildSelectable里返回true
         */
        return true;
    }

    /*
     * 分组监听
     */
    private void initialCheckBoxForGroup(GroupViewHolder holder, int groupPosition) {
        if (isVisible_batch_delete) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(isCheckedMap.get(groupList.get(groupPosition)));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
    }

    private void groupItemClicked(int groupPosition,  GroupViewHolder finalHolder, boolean isExpanded) {
        if (isVisible_batch_delete_child) {//如果处于孩子的批量删除界面，点击分组不应该有其他操作
            Toast.makeText(mContext, "当前界面此操作无效，请先点击取消", Toast.LENGTH_SHORT).show();
        } else if (isVisible_batch_delete) {//批量删除界面点击分组的操作
            finalHolder.checkBox.setChecked(!finalHolder.checkBox.isChecked());
            if (isCheckedMap.get(groupList.get(groupPosition))) {
                checkedItemNum--;
                isCheckedMap.put(groupList.get(groupPosition), false);
            } else {
                checkedItemNum++;
                isCheckedMap.put(groupList.get(groupPosition), true);
            }
            mainActivity.setSelectedItemnum(checkedItemNum);
        } else {//normal界面点击分组的操作
            if (isExpanded)
                Fragment_list.mListView.collapseGroup(groupPosition);
            else if (slideDeleteArrayList.size() == 0) {//还有侧滑删除的条目的时候，点击不应该展开，而是应该把那些侧滑删除条目回归
                Fragment_list.mListView.expandGroup(groupPosition, true);
                if (!groupList.get(groupPosition).isDiscover())
                    checkOnline(groupPosition);
            } else
                closeOtherItem();
        }
    }

    private boolean groupItemLongClicked() {
        collapseGroup();
        isVisible_batch_delete = true;
        fragment_list.set_isVisible_batch_delete(isVisible_batch_delete, isVisible_batch_delete_child);
        mainActivity.modelChange(isVisible_batch_delete, isVisible_batch_delete_child);
        initialCheckMap();
        checkedItemNum = 0;
        return false;
    }

    private void groupCheckBoxClicked(int groupPosition) {
        if (isCheckedMap.get(groupList.get(groupPosition))) {
            checkedItemNum--;
            isCheckedMap.put(groupList.get(groupPosition), false);
        }
        else {
            checkedItemNum++;
            isCheckedMap.put(groupList.get(groupPosition), true);
        }
        mainActivity.setSelectedItemnum(checkedItemNum);
    }

    private void groupShowState(int groupPosition, GroupViewHolder holder) {
        if (groupList.get(groupPosition).getTotalIp() == 0)//处理分组内不包含任何ip的情况
            groupList.get(groupPosition).setIsDiscover(false);
        if (groupList.get(groupPosition).isDiscover()) {
            Group group = groupList.get(groupPosition);
            int max = group.progressBarMax;
            if (group.isSnmpDis() && max == group.getProgress()) {
                group.setIsDiscover(false);
                holder.progressText.setVisibility(View.INVISIBLE);
                holder.progressBar.setVisibility(View.INVISIBLE);
            } else {
                holder.progressText.setVisibility(View.VISIBLE);
                if (!group.isSnmpDis())
                    holder.progressText.setText("正在ping设备：" + group.getProgress() + "/" + group.progressBarMax);
                else
                    holder.progressText.setText("正在识别设备类型：" + group.getProgress() + "/" + group.progressBarMax);
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.progressBar.setProgress(group.getProgress());
                holder.progressBar.setMax(max);
            }
        } else {
            holder.progressBar.setVisibility(View.INVISIBLE);
            holder.progressText.setVisibility(View.INVISIBLE);
        }
    }

    private void closeOtherItem() {
        // 采用Iterator的原因是for是线程不安全的，迭代器是线程安全的
        ListIterator<ViewGroup_SlideDelete> slideDeleteListIterator = slideDeleteArrayList.listIterator();
        while (slideDeleteListIterator.hasNext()) {
            ViewGroup_SlideDelete slideDelete = slideDeleteListIterator.next();
            slideDelete.isShowDelete(false);
        }
        slideDeleteArrayList.clear();
    }

    /*
     * 孩子监听
     */
    private void initialCheckBoxForChild(int groupPosition, int childPosition, ChildViewHolder holder) {
        if (isVisible_batch_delete_child) {//当处于可见状态时
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(isCheckedMap_child.get(mData.get(groupPosition).get(childPosition)));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
    }

    private void childItemClicked(int groupPosition, int childPosition, ChildViewHolder finalHolder) {
        if (isVisible_batch_delete_child) {
            finalHolder.checkBox.setChecked(!finalHolder.checkBox.isChecked());
            if (isCheckedMap_child.get(mData.get(groupPosition).get(childPosition))) {
                getCheckedItemNum_child--;
                isCheckedMap_child.put(mData.get(groupPosition).get(childPosition), false);
            } else {
                getCheckedItemNum_child++;
                isCheckedMap_child.put(mData.get(groupPosition).get(childPosition), true);
            }
            mainActivity.setSelectedItemnum_child(getCheckedItemNum_child);
        } else if (slideDeleteArrayList_forChild.size() != 0) {//还有侧滑删除项目，点击之后不应该动态加载
            closeOtherItemForChild();
        }
        else {
            fragment_list.dynamicLoad();
        }
    }

    private boolean childItemLongClicked(int groupPosition) {
        currentGroupIndex = groupPosition;
        collapseOtherGroups(groupPosition);
        isVisible_batch_delete_child = true;
        fragment_list.set_isVisible_batch_delete(isVisible_batch_delete, isVisible_batch_delete_child);
        mainActivity.modelChange(isVisible_batch_delete, isVisible_batch_delete_child);
        initialCheckMap_child(groupPosition);
        getCheckedItemNum_child = 0;
        return false;
    }

    private void childCheckBoxClicked(int groupPosition, int childPosition) {
        if (isCheckedMap_child.get(mData.get(groupPosition).get(childPosition))) {
            getCheckedItemNum_child--;
            isCheckedMap_child.put(mData.get(groupPosition).get(childPosition), false);
        } else {
            getCheckedItemNum_child++;
            isCheckedMap_child.put(mData.get(groupPosition).get(childPosition), true);
        }
        mainActivity.setSelectedItemnum_child(getCheckedItemNum_child);
    }

    private void closeOtherItemForChild() {
        ListIterator<ViewGroup_SlideDelete> slideDeleteListIterator = slideDeleteArrayList_forChild.listIterator();
        while (slideDeleteListIterator.hasNext()) {
            ViewGroup_SlideDelete slideDelete = slideDeleteListIterator.next();
            slideDelete.isShowDelete(false);
        }
        slideDeleteArrayList_forChild.clear();
    }

    private void collapseGroup() {
        for (int i = 0; i < groupList.size(); i++) {
            Fragment_list.mListView.collapseGroup(i);
        }
    }


    //检测设备是否在线
    private void checkOnline(int groupPosition) {//点击展开分组时触发，根据设备在线离线情况设置设备的online属性，以便之后根据online设置childItem字体颜色
        List<Child> childList = mData.get(groupPosition);
        for (int i = 0; i < childList.size(); i++) {
            IpTask1 ipTask1 = new IpTask1(i, childList);
            ipTask1.executeOnExecutor(LIMIT_TASK_EXECUTOR, childList.get(i).getIp());
        }
    }

    /*
            侧滑编辑删除分组
     */
    public void removeItem(int position) {  //侧滑之后点击删除按钮，根据groupPosition来删除分组，并且更新数据库
        long pos = Fragment_list.mListView.getExpandableListPosition(position);
        int groupPos = ExpandableListView.getPackedPositionGroup(pos);// 获取group的id
        String group_name_to_del = groupList.get(groupPos).getGroupName();
        groupList.remove(groupPos);
        mData.remove(groupPos);
        notifyDataSetChanged();
//        String sql_delete_group_item="delete from group_info where _id="+(groupPos+1);//不能直接根据id来删除，因为删除之后的空缺是不会自动补上的，这样如果删除了之前的元素
        //再往后删除就会出问题
        String sql_delete_group_item = "delete from group_info where group_name='" + group_name_to_del + "'";
        String sql_delete_group_ip_seg = "delete from ip_seg_info where group_name='" + group_name_to_del + "'";

        MainActivity.dbHelper.getWritableDatabase().execSQL(sql_delete_group_item);//删除分组数据库中这个分组的数据
        MainActivity.dbHelper.getWritableDatabase().execSQL(sql_delete_group_ip_seg);//删除ip段数据库中这个分组的数据

    }

    /*
            侧滑编辑删除孩子
     */
    public void removeItem_child(int position, int child_position) {
        long pos = Fragment_list.mListView.getExpandableListPosition(position);
        int groupPos = ExpandableListView.getPackedPositionGroup(pos);// 获取group的id
        List<Child> childs = mData.get(groupPos);
        String ip_to_del = childs.get(child_position).getIp();
        String group_name = groupList.get(groupPos).getGroupName();
        for (int i = 0; i < childs.size(); i++) {
            if (childs.get(i).getIp().equals(ip_to_del)) {
                childs.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
        //删除数据加入删除信息表
        MainActivity.dbHelper.getWritableDatabase().execSQL("insert into deleted_devices_info values(null,?,?)", new String[]{group_name, ip_to_del});
    }

    public void setOnEditGroupClickListener(OnEditGroupClickListener onEditGroupClickListener) {
        this.onEditGroupClickListener = onEditGroupClickListener;
    }

    private void collapseOtherGroups(int groupPosition) {
        for (int i = 0; i < groupList.size(); i++) {
            if (i != groupPosition) {
                Fragment_list.mListView.collapseGroup(i);
            }
        }
    }

    /*
            批量删除分组
     */
    private void initialCheckMap() {
        isCheckedMap = new HashMap<>();
        for (int i = 0; i < groupList.size(); i++) {
            isCheckedMap.put(groupList.get(i), false);
        }
    }

    public void removeItems() {
        for (int i = 0; i < groupList.size(); i++) {
            if (isCheckedMap.get(groupList.get(i))) {
                removeItem(i);
                i--;
            }
        }
    }
    /*
              批量删除孩子
     */
    private void initialCheckMap_child(int groupPosition) {
        isCheckedMap_child = new HashMap<>();
        List<Child> children = mData.get(groupPosition);
        int size = children.size();
        for (int i = 0; i < size; i++) {
            isCheckedMap_child.put(children.get(i), false);
        }
    }

    public void setIsVisible_batch_delete(boolean isVisible_batch_delete, boolean isVisible_batch_delete_child) {
        this.isVisible_batch_delete = isVisible_batch_delete;
        this.isVisible_batch_delete_child = isVisible_batch_delete_child;
    }

    public void removeItems_child() {
        List<Child> childs = mData.get(currentGroupIndex);
        String group_name = groupList.get(currentGroupIndex).getGroupName();
        for (int i = 0; i < childs.size(); i++) {
            if (isCheckedMap_child.get(childs.get(i))) {
                String ip_to_del = childs.get(i).getIp();
                childs.remove(i);
                i--;
                MainActivity.dbHelper.getWritableDatabase().execSQL("insert into deleted_devices_info values(null,?,?)", new String[]{group_name, ip_to_del});
            }
        }
        notifyDataSetChanged();
    }

    private class GroupViewHolder {
        TextView mGroupName;
        TextView mGroupCount;
        ProgressBar progressBar;
        TextView progressText;
        CheckBox checkBox;
        LinearLayout mElvContent;
        LinearLayout mEdit;
        LinearLayout mDelete;
        ViewGroup_SlideDelete mSlideDelete;
    }

    private class ChildViewHolder {
        ImageView mIcon;
        TextView mChildName;
        TextView mDetail;
        CheckBox checkBox;
        LinearLayout mDelete;
        LinearLayout mEdit;
        LinearLayout mElvContent_child;
        ViewGroup_SlideDelete mSlideDelete;

    }

    public interface OnEditGroupClickListener {//回调接口，为了MainActivity中与其他Acvitity进行通信
        void onEditGroupClick(int groupPosition);
    }

    public interface OnLongClickListener {//回调接口，为了Fragment_list中进行长按之后的界面变换
        void modelChange(boolean isVisible, boolean isVisible_child);
    }

    private static class IpTask1 extends AsyncTask<String, Void, Boolean> {
        int seq;
        List<Child> childList;

        public IpTask1(int seq, List<Child> childList) {
            this.seq = seq;
            this.childList = childList;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                address = InetAddress.getByName(strings[0]);
                int num = 0;
                for (int i = 0; i < 1; i++) {
                    if (address.isReachable(5000))
                        num++;
                }
                if (num != 0) {
                    childList.get(seq).setOnline(true);
                    return true;
                } else {
                    childList.get(seq).setOnline(false);
                    return false;
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
