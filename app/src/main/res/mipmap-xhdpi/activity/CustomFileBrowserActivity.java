package com.KryptosTextApp.KryptosText.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.model.FileItem;
import com.KryptosTextApp.KryptosText.utils.CommonUtils;
import com.KryptosTextApp.KryptosText.utils.PHCDateFormatter;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomFileBrowserActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {
    RecyclerView rv_records;
    ArrayList<FileItem> records;
    CustomAdapter customAdapter;
    Context context;

    String[] supportedTypes = new String[]{"DOC", "DOCX", "PDF", "PPT", "TXT", "CSV", "XLS", "XLSX"};

    private MaterialSearchView materialSearchView;
    private View tv_no_documents;
//    String[] imageFormats = new String[]{"ANI", "BMP", "CAL", "FAX", "GIF", "IMG", "JBG", "JPE", "JPEG", "JPG", "MAC", "PBM"
//            , "PCD", "PCX", "PCT", "PGM", "PNG", "PPM", "PSD", "RAS", "TGA", "TIFF", "WMF"};
//    String[] videoFormats = new String[]{"FLV", "AVI", "MOV", "MP4", "ASF", "MPG", "WMV", "3GP", "RM", "SWF"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_file_browser);
        context = com.KryptosTextApp.KryptosText.activity.CustomFileBrowserActivity.this;
        loadToolbar();
        materialSearchView = (MaterialSearchView) findViewById(R.id.search_view);

        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });

        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });


        records = new ArrayList<>();
        new GetDocumentTask().execute();
        rv_records = (RecyclerView) findViewById(R.id.rv_records);
        tv_no_documents = findViewById(R.id.tv_no_documents);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rv_records.setLayoutManager(layoutManager);
        customAdapter = new CustomAdapter(records);
        rv_records.setAdapter(customAdapter);
    }

    private void loadToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mToolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
        mToolbarTitle.setVisibility(View.VISIBLE);
        mToolbarTitle.setText(getResources().getString(R.string.documents));
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fragment_conversations, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        materialSearchView.setMenuItem(item);
        return true;
    }

    private void search(String searchString) {
        if (customAdapter != null) {
            customAdapter.getFilter().filter(searchString);
        }
    }

    class GetDocumentTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Void... voids) {
            getFiles(null);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (records.size() == 0) {
                tv_no_documents.setVisibility(View.VISIBLE);
                rv_records.setVisibility(View.GONE);
            } else {
                tv_no_documents.setVisibility(View.GONE);
                rv_records.setVisibility(View.VISIBLE);
            }
            if (progressDialog != null)
                progressDialog.dismiss();
            if (customAdapter != null)
                customAdapter.notifyDataSetChanged();

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(com.KryptosTextApp.KryptosText.activity.CustomFileBrowserActivity.this);
            progressDialog.setMessage("Processing");
            progressDialog.show();
        }
    }

    class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
        ArrayList<FileItem> fileItems;

        public CustomAdapter(ArrayList<FileItem> fileItems) {
            this.fileItems = fileItems;
        }

        @Override
        public Filter getFilter() {
            return new ConversationsFilter();
        }

        public class ConversationsFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    // No filter implemented we return all the list
                    filterResults.values = fileItems;
                    filterResults.count = fileItems.size();
                } else {
                    List<FileItem> filteredList = new ArrayList<>();
                    for (int i = 0; i < fileItems.size(); i++) {
                        FileItem fileItem = fileItems.get(i);
                        if (fileItem.getFileName().toUpperCase().contains(constraint.toString().toUpperCase())) {
                            filteredList.add(fileItem);
                        }
                    }
                    filterResults.values = filteredList;
                    filterResults.count = filteredList.size();
                }
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {

			/*if (results.count == 0) {
                notifyDataSetInvalidated();
	        } else {*/
                records = (ArrayList<FileItem>) results.values;
                notifyDataSetChanged();
                //AddGroup.setListViewHeightBasedOnChildren(mListView);

                //}
            }
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvText, tvSize, tvDate;
            private LinearLayout ll_item;
            private ImageView iv_thumbnail;

            public MyViewHolder(View view) {
                super(view);
                tvText = (TextView) view.findViewById(R.id.tv_title);
                tvSize = (TextView) view.findViewById(R.id.tv_size);
                tvDate = (TextView) view.findViewById(R.id.tv_date);
                ll_item = (LinearLayout) view.findViewById(R.id.ll_item);
                iv_thumbnail = (ImageView) view.findViewById(R.id.iv_folder_type);
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_browser_grid, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final MyViewHolder holder1 = (MyViewHolder) holder;
            holder1.tvText.setText(records.get(position).getFileName());
            holder1.tvSize.setText(CommonUtils.fileSize(records.get(position).getFile().length()));
            holder1.tvDate.setText(PHCDateFormatter.getDateFromMillis(records.get(position).getFile().lastModified()));

//            if (records.get(position) != null && records.get(position).getFileExt() != null && (Arrays.asList(videoFormats).contains(records.get(position).getFileExt()) || Arrays.asList(imageFormats).contains(records.get(position).getFileExt()))) {
//                Glide.with(context)
//                        .load(records.get(position).getFilePath()).into(holder1.iv_thumbnail);
//            } else
            holder1.iv_thumbnail.setImageDrawable(getDrawableVal(records.get(position)));

            holder1.ll_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(CustomFileBrowserActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
//                    if (records.get(position).isDirectory()) {
//                        getFiles(records.get(position).getFilePath());
//                    } else
//                        Toast.makeText(CustomFileBrowserActivity.this, "Not Supported", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("resultURI", records.get(position).getFile().toURI().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return records.size();
        }
    }

    private Drawable getDrawableVal(FileItem fileItem) {
        if (fileItem.getFileExt() != null) {
            switch (fileItem.getFileExt().toLowerCase()) {
                case "doc":
                case "docx":
                    return getResources().getDrawable(R.drawable.img_word_document);
                case "pdf":
                    return getResources().getDrawable(R.drawable.img_pdf);
                case "xls":
                case "xlsx":
                case "excel":
                    return getResources().getDrawable(R.drawable.img_excel);
                case "ppt":
                    return getResources().getDrawable(R.drawable.img_ppt);
                case "txt":
                    return getResources().getDrawable(R.drawable.img_txt);
                case "csv":
                    return getResources().getDrawable(R.drawable.img_csv);
                default:
                    return getResources().getDrawable(R.drawable.img_otherdoc);
            }
        }
        return getResources().getDrawable(R.drawable.ic_folder);
    }

    private void getFiles(String pathname) {
        File sd;
        File[] files, directories;
        if (pathname == null)
            sd = Environment.getExternalStorageDirectory();
        else
            sd = new File(pathname);

        files = sd.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
//                return file.isDirectory() || Arrays.asList(supportedTypes).contains(FilenameUtils.getExtension(file.getName()).toUpperCase()) || Arrays.asList(videoFormats).contains(FilenameUtils.getExtension(file.getName()).toUpperCase()) || Arrays.asList(imageFormats).contains(FilenameUtils.getExtension(file.getName()).toUpperCase());
                return Arrays.asList(supportedTypes).contains(FilenameUtils.getExtension(file.getName()).toUpperCase());
            }
        });
        if (files != null)
            for (int i = 0; i < files.length; i++) {
                records.add(new FileItem(files[i]));
            }
        directories = sd.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        if (directories != null)
            for (int i = 0; i < directories.length; i++) {
                getFiles(directories[i].getAbsolutePath());
            }
    }
}
