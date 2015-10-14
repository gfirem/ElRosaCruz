package com.gfirem.elrosacruz.entity;

import com.gfirem.elrosacruz.MainActivity;
import com.gfirem.elrosacruz.R;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class InfoWindowMap implements InfoWindowAdapter{
	Context context;

	public InfoWindowMap(Context context) {
		this.context = context;
	}

	@Override
	public View getInfoContents(Marker mark) {
		View v = ((MainActivity) context).getLayoutInflater().inflate(R.layout.view_info_in_map, null);
        TextView txtTitle = (TextView) v.findViewById(R.id.txtTitle);
        txtTitle.setText(mark.getTitle());
        return v;
	}

	@Override
	public View getInfoWindow(Marker mark) {
		return null;
	}

}
