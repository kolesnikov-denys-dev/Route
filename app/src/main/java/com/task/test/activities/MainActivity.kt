package com.task.test.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.task.test.R
import com.task.test.adapters.ViewPagerAdapter
import com.task.test.fragments.MapFragment
import com.task.test.fragments.OnDataPass
import com.task.test.model.LocationWithName
import com.task.test.util.Constants.KEY_FROM
import com.task.test.util.Constants.KEY_IS_FROM
import com.task.test.util.Constants.KEY_TO
import com.task.test.viewmodels.MapFragmentViewModel
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnDataPass {

    private var fromIsSelected = false
    private var toIsSelected = false
    private lateinit var locationWithNameFrom: LocationWithName
    private lateinit var locationWithNameTo: LocationWithName

    private lateinit var viewModel: MapFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MapFragmentViewModel::class.java)

        val fromFragment = MapFragment()
        val argsFrom = Bundle()
        argsFrom.putBoolean(KEY_IS_FROM, true)
        fromFragment.arguments = argsFrom

        val toFragment = MapFragment()
        val argsTo = Bundle()
        argsTo.putBoolean(KEY_IS_FROM, false)
        toFragment.arguments = argsTo

        viewPager2.adapter = ViewPagerAdapter(this, listOf<Fragment>(fromFragment, toFragment))
        viewPager2.isUserInputEnabled = false

        TabLayoutMediator(tab_layout, viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.from)
                1 -> tab.text = getString(R.string.to)
            }
        }.attach()

        routeButton.setOnClickListener {
            if (fromIsSelected && toIsSelected) {
                Toast.makeText(this, getString(R.string.build_route), Toast.LENGTH_LONG).show()
                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra(KEY_FROM, locationWithNameFrom)
                intent.putExtra(KEY_TO, locationWithNameTo)
                startActivity(intent)
            } else if (fromIsSelected && !toIsSelected) {
                Toast.makeText(this, getString(R.string.select_to), Toast.LENGTH_LONG).show()
            } else if (!fromIsSelected && toIsSelected) {
                Toast.makeText(this, getString(R.string.select_from), Toast.LENGTH_LONG).show()
            } else if (!fromIsSelected && !toIsSelected) {
                Toast.makeText(this, getString(R.string.select_from_to), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDataPass(locationWithName: LocationWithName) {
        if (locationWithName.from) {
            fromIsSelected = true
            locationWithNameFrom = locationWithName
        } else {
            toIsSelected = true
            locationWithNameTo = locationWithName
        }
    }

}