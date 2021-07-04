package com.example.testapps

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testapps.adapters.ApplicationsAdapter
import com.example.testapps.databinding.FragmentFirstBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var listAdapter: ApplicationsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getApplicationList()

        binding.apply {
            listAdapter = ApplicationsAdapter {
                //startActivity(it.packageName)
                sendNotifications(createStartPackageendingIntent(it.packageName), "trigger", "open app" + it.appName, notificationId = (Math.random() * 1000).toInt())
            }

            list.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            filter.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    refreshApps(s.toString())
                }
            })
        }

        refreshApps(null)

    }

    fun refreshApps(filter: String?) {
        viewLifecycleOwner.lifecycleScope.launch (Dispatchers.Default) {
            val appInfoList = getAppInfo(filter)
            withContext(Dispatchers.Main) {
                listAdapter?.submitList(appInfoList)
            }
        }
    }

    fun getAppInfo(fillter: String?): List<AppInfo> {
        return getApplicationListResulver().filter {
            it.packageName.contains(fillter ?: "") ||
                    it.appName.contains(fillter ?: "")
        }
    }


    fun getApplicationList() : List<ApplicationInfo> {
        val pm: PackageManager = requireContext().packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
    }

    fun getApplicationListResulver(): List<AppInfo> {
        val pm: PackageManager = requireContext().packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pkgAppsList = requireContext().packageManager.queryIntentActivities(mainIntent, 0)
        return pkgAppsList.map {
            AppInfo(
                it.loadLabel(pm).toString(),
                it.activityInfo.packageName,
                it.loadIcon(pm)
            )
        }
    }

    fun startActivity(packageName: String) {
        val pm: PackageManager = requireContext().packageManager
        val launchIntent: Intent? = pm.getLaunchIntentForPackage(packageName)
        startActivity(launchIntent)
    }


    fun sendNotifications(action: PendingIntent, notificationTitle: String, notificationMessage: String, notificationId: Int? = null) {
        val notificationHelper = NotificationHelper(requireContext())
        notificationHelper.sendNotification(notificationTitle, notificationMessage, action, notificationId = notificationId)
    }

    fun createStartPackageendingIntent(packageName: String): PendingIntent {
        val pm: PackageManager = requireContext().packageManager
        val launchIntent: Intent? = pm.getLaunchIntentForPackage(packageName)
        launchIntent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return PendingIntent.getActivity(context, 0, launchIntent, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG = FirstFragment.javaClass.simpleName
    }
}

data class AppInfo(
    val appName: String,
    val packageName: String,
    val appIcon: Drawable
)