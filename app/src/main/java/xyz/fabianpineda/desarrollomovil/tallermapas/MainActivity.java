package xyz.fabianpineda.desarrollomovil.tallermapas;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, AuxiliarPermisos.HandlerPermisoConcedido {
    private boolean permisoGPS;
    private boolean permisoHTTP;

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private boolean mapaListo;

    private AuxiliarPermisos auxiliarPermisos;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng ubicacionUTB = new LatLng(10.370337, -75.465449);
        LatLngBounds utbBounds = new LatLngBounds(new LatLng(10.368202, -75.466161), new LatLng(10.371140, -75.464468));
        CameraPosition inicial = (new CameraPosition.Builder()).target(utbBounds.getCenter()).zoom(18.5f).bearing(140f).build();

        map = googleMap;

        if (!permisoGPS) {
            //map.setMyLocationEnabled(false);
        }

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMinZoomPreference(18.5f);
        map.setMaxZoomPreference(20.0f);
        map.setLatLngBoundsForCameraTarget(utbBounds);

        map.addMarker(new MarkerOptions().position(ubicacionUTB)).setVisible(true);

        map.moveCamera(CameraUpdateFactory.newCameraPosition(inicial));
    }

    @Override
    public void permisoConcedido(String permiso) {
        switch(permiso) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                permisoGPS = true;
                break;
            case Manifest.permission.ACCESS_NETWORK_STATE:
                permisoHTTP = true;
                break;
            default:
                return;
        }

        if (!mapaListo) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void permisoDenegado(String permiso) {
        switch(permiso) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                permisoGPS = false;
                break;
            case Manifest.permission.ACCESS_NETWORK_STATE:
                permisoHTTP = false;
                break;
            default:
                return;
        }

        if (!mapaListo) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_gps:
                auxiliarPermisos.pedirPermiso(Manifest.permission.ACCESS_FINE_LOCATION, getString(R.string.antes_pedir_permisos));
                break;
            case R.id.menu_http:
                auxiliarPermisos.pedirPermiso(Manifest.permission.ACCESS_NETWORK_STATE, getString(R.string.antes_pedir_permisos));
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mapa, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permisoGPS = false;
        permisoHTTP = false;
        auxiliarPermisos = new AuxiliarPermisos(this, this);

        mapaListo = false;
        mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.activity_main, mapFragment).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            // ...
        } else {
            // ...
        }
    }
}
