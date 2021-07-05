package com.example.testapps

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
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
import com.example.testapps.adapters.PhonesAdapter
import com.example.testapps.databinding.ChoosePhoneDialogBinding
import com.example.testapps.helpers.setFullScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable


class ChoosePhoneDialog: DialogFragment() {

    private var _binding: ChoosePhoneDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var listAdapter: PhonesAdapter? = null

    private var phoneInfoList: List<PhoneInfo>? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setFullScreen()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChoosePhoneDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            listAdapter = PhonesAdapter {
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
                    refreshContants(s.toString())
                }
            })

            back.setOnClickListener {
                dismiss()
            }
        }

        refreshContants(null)

    }

    fun setResult(phoneInfo: PhoneInfo) {
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResult(PHONE_Request_Key, bundleOf(PhoneInfo_Key to phoneInfo))
        dismiss()
    }

    fun refreshContants(filter: String?) {
        viewLifecycleOwner.lifecycleScope.launch (Dispatchers.Default) {
            val phoneInfoList = getPhoneInfo(filter)
            withContext(Dispatchers.Main) {
                listAdapter?.submitList(phoneInfoList)
            }
        }
    }

    fun getPhoneInfo(fillter: String?): List<PhoneInfo> {
        return getContacts(requireContext()).filter {
            it.name.contains(fillter ?: "")
        }
    }


    fun getContacts(ctx: Context): List<PhoneInfo> {
        val list: MutableList<PhoneInfo> = ArrayList()
        val contentResolver: ContentResolver = ctx.getContentResolver()
        val cursor: Cursor =
            contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)!!
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                val id: String =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val cursorInfo: Cursor? = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
//                    val inputStream: InputStream =
//                        ContactsContract.Contacts.openContactPhotoInputStream(
//                            ctx.getContentResolver(),
//                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLong())
//                        )
                    val person: Uri =
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLong())
                    val pURI: Uri = Uri.withAppendedPath(
                        person,
                        ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
                    )
//                    var photo: Bitmap? = null
//                    if (inputStream != null) {
//                        photo = BitmapFactory.decodeStream(inputStream)
//                    }
                    while (cursorInfo?.moveToNext() == true) {
                        val info = PhoneInfo(
                            id,
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)),
                            cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                            pURI
                        )
                        list.add(info)
                    }
                    cursorInfo?.close()
                }
            }
            cursor.close()
        }
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val PHONE_Request_Key = "phone result key"
        val PhoneInfo_Key = "phone info key"
        fun getResult(bundle: Bundle): PhoneInfo {
            return bundle.getSerializable(PhoneInfo_Key) as PhoneInfo
        }
    }
}

data class PhoneInfo(
    val id: String,
    val name: String,
    val mobileNumber: String,
    val photoURI: Uri
): Serializable