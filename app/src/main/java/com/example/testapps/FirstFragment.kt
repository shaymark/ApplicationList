package com.example.testapps

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.testapps.databinding.FragmentFirstBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            applicationBtn.setOnClickListener {
                ApplicationDialog().show(parentFragmentManager, null)
            }

            phoneBtn.setOnClickListener {
                ChoosePhoneDialog().show(parentFragmentManager, null)
            }
        }

        setFragmentResultListener(ApplicationDialog.APP_Request_Key) { requestKey, bundle ->
            val appInfo = ApplicationDialog.getResult(bundle)
            sendNotification(appInfo)
        }

        setFragmentResultListener(ChoosePhoneDialog.PHONE_Request_Key) { requestKey, bundle ->
            val phoneInfo = ChoosePhoneDialog.getResult(bundle)
            Log.d("OnViewCreate", "onViewCreated: ${phoneInfo.name} - ${phoneInfo.mobileNumber}")
        }
    }

    fun sendNotification(appInfo: AppInfo) {
        val pendingIntent = createStartPackageendingIntent(appInfo.packageName)
        sendNotifications(pendingIntent,
            "trigger",
            "open app" + appInfo.appName,
            notificationId = (Math.random() * 1000).toInt())

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
}