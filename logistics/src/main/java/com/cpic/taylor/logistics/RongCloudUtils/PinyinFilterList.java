package com.cpic.taylor.logistics.RongCloudUtils;

import android.text.TextUtils;
import android.widget.Filter;

import com.cpic.taylor.logistics.RongCloudModel.IFilterModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public abstract class PinyinFilterList<T extends IFilterModel> extends Filter {

    private List<T> mOriginalDatas;

    private String mKeyIndex;

    private static final String INDEX_FORMATTER = "|%1$d:%2$s|";

    private static final String REGEX_TEMPLATE = "\\|(\\d+):([^|]*?%1$s)\\|";
    private static final String REGEX_KEY_TEMPLATE = "[^|]*?";

    public PinyinFilterList(List<T> dataList) {
        mOriginalDatas = dataList;

        refreshIndex();
    }

    private void refreshIndex() {
        StringBuilder keysBuilder = new StringBuilder();

        if (mOriginalDatas != null && mOriginalDatas.size() > 0) {
            for (int i = 0; i < mOriginalDatas.size(); i++) {
                T t = mOriginalDatas.get(i);
                keysBuilder.append(String.format(INDEX_FORMATTER, i, t.getFilterKey().toLowerCase(Locale.getDefault())));
            }
        }
        mKeyIndex = keysBuilder.toString();
    }

    /**
     * @return the mOriginalDatas
     */
    public List<T> getOriginalDatas() {
        return mOriginalDatas;
    }

    /**
     * @param mOriginalDatas the mOriginalDatas to set
     */
    public void setOriginalDatas(List<T> mOriginalDatas) {
        this.mOriginalDatas = mOriginalDatas;
        refreshIndex();
    }

    @Override
    protected FilterResults performFiltering(CharSequence c) {
        FilterResults results = new FilterResults();
        String constraint = c.toString().trim().toLowerCase(Locale.getDefault());

        if (TextUtils.isEmpty(constraint)) {
            results.values = mOriginalDatas;
            results.count = mOriginalDatas.size();
            return results;
        }

        if (mKeyIndex == null && mKeyIndex.length() == 0)
            return results;

        StringBuilder keysRegexBuilder = new StringBuilder();

        for (int i = 0; i < constraint.length(); i++) {
            keysRegexBuilder.append(constraint.charAt(i));
            keysRegexBuilder.append(REGEX_KEY_TEMPLATE);
        }

        String regexString = String.format(REGEX_TEMPLATE, keysRegexBuilder.toString());

        Pattern p = Pattern.compile(regexString);
        Matcher matcher = p.matcher(mKeyIndex);
        LinkedList<T> resultList = new LinkedList<T>();
        if (matcher.find()) {
            do {
                int index = Integer.valueOf(matcher.group(1));
                resultList.add(mOriginalDatas.get(index));
            } while (matcher.find());
        }

        results.values = resultList;
        results.count = resultList.size();
        return results;
    }

}
