package com.cpic.taylor.logistics.RongCloudFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.Conversation;

/**
 * Created by Administrator on 2015/3/6.
 */
public class ChatRoomListFragment extends Fragment implements AdapterView.OnItemClickListener {

    /**
     * 聊天室的 ListView
     */
    private ListView mListView;

    /**
     * 数据适配器
     */
    private MyAdapter mAdapter;

    /**
     * 填充数据的集合
     */
    private List<ChatRoomBean> mList;

    public MyHolder mViewHolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.de_fr_chatroom_list, container, false);
        mListView = (ListView) view.findViewById(R.id.lv_chatroomlist);
        initData();
        mAdapter = new MyAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (RongIM.getInstance() != null) {
            switch (position) {

                case 0:
                    RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, "chatroom001", "融云客服");
                    break;
                case 1:
                    RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, "chatroom002", "Caskia");
                    break;
                case 2:
                    RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, "chatroom003", "大象设计");
                    break;
                case 3:
                    RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, "chatroom004", "DaveDing");
                    break;
                case 4:
                    RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, "chatroom005", "lovry");
                    break;
            }
        }
    }

    /**
     * 填充数据
     */
    private void initData() {
        ChatRoomBean chatRoomBean1 = new ChatRoomBean();
        ChatRoomBean chatRoomBean2 = new ChatRoomBean();
        ChatRoomBean chatRoomBean3 = new ChatRoomBean();
        ChatRoomBean chatRoomBean4 = new ChatRoomBean();
        ChatRoomBean chatRoomBean5 = new ChatRoomBean();
        chatRoomBean1.setChatRoomIcon(R.drawable.icon1);
        chatRoomBean2.setChatRoomIcon(R.drawable.icon2);
        chatRoomBean3.setChatRoomIcon(R.drawable.icon3);
        chatRoomBean4.setChatRoomIcon(R.drawable.icon4);
        chatRoomBean5.setChatRoomIcon(R.drawable.icon5);

        chatRoomBean1.setTiemTitle("融云客服");
        chatRoomBean2.setTiemTitle("Caskia");
        chatRoomBean3.setTiemTitle("大象设计");
        chatRoomBean4.setTiemTitle("DaveDing");
        chatRoomBean5.setTiemTitle("lovry");

        chatRoomBean1.setDescribeA("#在线服务#");
        chatRoomBean2.setDescribeA("#云服务#");
        chatRoomBean3.setDescribeA("#设计#");
        chatRoomBean4.setDescribeA("#时尚#");
        chatRoomBean5.setDescribeA("#文艺#");

        chatRoomBean1.setDescribeB("多年专注于移动互联网即时通");
        chatRoomBean2.setDescribeB("单聊群聊多种使用场景");
        chatRoomBean3.setDescribeB("提供文字表情防语音片段等...");
        chatRoomBean4.setDescribeB("各类时尚资讯");
        chatRoomBean5.setDescribeB("多年专注于移动互联网即时通...");

        mList = new ArrayList<ChatRoomBean>();
        mList.add(chatRoomBean1);
        mList.add(chatRoomBean2);
        mList.add(chatRoomBean3);
        mList.add(chatRoomBean4);
        mList.add(chatRoomBean5);

    }


    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            mViewHolder = new MyHolder();
            if (convertView != null ) {
                mViewHolder = (MyHolder) convertView.getTag();
            } else {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.de_item_chatroom, parent,false);
                mViewHolder.mIV_Icon = (AsyncImageView) convertView.findViewById(R.id.iv_chatroom_icon);
                mViewHolder.mTV_Title = (TextView) convertView.findViewById(R.id.tv_chatroom_title);
                mViewHolder.mTV_DescribeA = (TextView) convertView.findViewById(R.id.tv_chatroom_d_a);
                mViewHolder.mTV_DescribeB = (TextView) convertView.findViewById(R.id.tv_chatroom_d_b);
                convertView.setTag(mViewHolder);
            }
            if (mViewHolder != null) {
                mViewHolder.mIV_Icon.setImageResource(mList.get(position).getChatRoomIcon());
                mViewHolder.mTV_Title.setText(mList.get(position).getTiemTitle());
                mViewHolder.mTV_DescribeA.setText(mList.get(position).getDescribeA());
                mViewHolder.mTV_DescribeB.setText(mList.get(position).getDescribeB());
            }
            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    /**
     * chatRoom数据的实体类
     */
    class ChatRoomBean {

        private int chatRoomIcon;

        private String tiemTitle;

        private String describeA;

        private String describeB;

        public int getChatRoomIcon() {
            return chatRoomIcon;
        }

        public void setChatRoomIcon(int chatRoomIcon) {
            this.chatRoomIcon = chatRoomIcon;
        }

        public String getTiemTitle() {
            return tiemTitle;
        }

        public void setTiemTitle(String tiemTitle) {
            this.tiemTitle = tiemTitle;
        }

        public String getDescribeA() {
            return describeA;
        }

        public void setDescribeA(String describeA) {
            this.describeA = describeA;
        }

        public String getDescribeB() {
            return describeB;
        }

        public void setDescribeB(String describeB) {
            this.describeB = describeB;
        }

    }


    /**
     * 减少子控件查询次数的 viewhodler
     */
    public class MyHolder {
        public AsyncImageView mIV_Icon;
        public TextView mTV_Title;
        public TextView mTV_DescribeA;
        public TextView mTV_DescribeB;
    }
}
