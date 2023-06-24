package cz.nejakejtomas.bluemapbanners.markers

import cz.nejakejtomas.bluemapbanners.markers.banners.Banner
import cz.nejakejtomas.bluemapbanners.markers.maps.Map

interface MarkerVisitor<Out, Context> {
    fun visit(marker: Marker, context: Context): Out
    fun visit(banner: Banner, context: Context): Out
    fun visit(map: Map, context: Context): Out
}