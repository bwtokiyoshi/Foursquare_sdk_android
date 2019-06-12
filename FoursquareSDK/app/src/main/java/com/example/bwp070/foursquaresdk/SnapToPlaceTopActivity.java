package com.example.bwp070.foursquaresdk;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SnapToPlaceTopActivity extends AppCompatActivity {

    private SnapToPlaceOpenHelper snapToPlaceHelper;
    private ListView listView;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap_to_place_top);

        // DB作成
        snapToPlaceHelper = new SnapToPlaceOpenHelper(getApplicationContext());

        // 変数textViewに表示するテキストビューのidを格納
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView = findViewById(R.id.snap_to_place_list);
        listView.setFastScrollEnabled(true);
        listView.setAdapter(mAdapter);

        //readData();
    }

//    /**
//     * DBからデータを全件取得し画面に表示する.
//     * @param
//     */
//    public void readData(){
//        mAdapter.clear();
//
//        SQLiteDatabase db = snapToPlaceHelper.getReadableDatabase();
//        Cursor cursor = db.query(
//                "foursquaredb",
//                new String[] { "title", "score" },
//                null,
//                null,
//                null,
//                null,
//                null
//        );
//
//        cursor.moveToFirst();
//
//        StringBuilder sbuilder = new StringBuilder();
//
//        for (int i = 0; i < cursor.getCount(); i++) {
//            sbuilder.append(cursor.getString(0));
//            sbuilder.append(":    ");
//            sbuilder.append(cursor.getInt(1));
//            sbuilder.append("点\n\n");
//            cursor.moveToNext();
//        }
//
//        if(!TextUtils.isEmpty(sbuilder)) {
//            String[] splitsStr = sbuilder.split("\n\n");
//            mAdapter.addAll(splitsStr);
//        }else{
//            mAdapter.add("表示済み通知はありません");
//        }
//
//        cursor.close();
//
//        listView.(sbuilder.toString());
//    }
}
