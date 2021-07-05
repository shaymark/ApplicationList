package com.example.testapps

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
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testapps.adapters.ApplicationsAdapter
import com.example.testapps.databinding.ChooseApplicationDialogBinding
import com.example.testapps.helpers.setFullScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

class ApplicationDialog: DialogFragment() {

    private var _binding: ChooseApplicationDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var listAdapter: ApplicationsAdapter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setFullScreen()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChooseApplicationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getApplicationList()

        binding.apply {
            listAdapter = ApplicationsAdapter {
                setResult(it)
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

            back.setOnClickListener {
                dismiss()
            }
        }

        refreshApps(null)

    }

    fun setResult(appInfo: AppInfo) {
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResult(APP_Request_Key, bundleOf(AppInfo_Key to appInfo))
        dismiss()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val APP_Request_Key = "app result key"
        val AppInfo_Key = "app info key"
        fun getResult(bundle: Bundle): AppInfo {
            return bundle.getSerializable(AppInfo_Key) as AppInfo
        }
    }
}

data class AppInfo(
    val appName: String,
    val packageName: String,
    val appIcon: Drawable
): Serializable