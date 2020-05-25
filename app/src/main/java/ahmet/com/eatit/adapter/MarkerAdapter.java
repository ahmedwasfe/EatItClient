package ahmet.com.eatit.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import ahmet.com.eatit.R;

public class MarkerAdapter implements GoogleMap.InfoWindowAdapter {

    private View itemVIew;

    public MarkerAdapter(LayoutInflater inflater){

        itemVIew = inflater.inflate(R.layout.layout_marker, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView mTxtShipperName = itemVIew.findViewById(R.id.txt_marker_sshipper_name);
        TextView mTxtShipperInfo = itemVIew.findViewById(R.id.txt_marker_sshipper_info);

        mTxtShipperName.setText(marker.getTitle());
        mTxtShipperInfo.setText(marker.getSnippet());

        return itemVIew;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
