package com.sycorax.ourquiz

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class WaitingForPlayersHostSection : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_waiting_for_players_host_section, container, false)
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            WaitingForPlayersHostSection().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
