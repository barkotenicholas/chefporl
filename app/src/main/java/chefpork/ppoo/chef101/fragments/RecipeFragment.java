package chefpork.ppoo.chef101.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chef101.R;
import chefpork.ppoo.chef101.pojo.Recipe;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipeFragment extends Fragment implements PurchasesUpdatedListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static BillingClient billingClient;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static final List<String> LIST_OF_SKUS = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add("mango");
                add("mine");
                add("pineapple");
            }});
   // public BillingClient billingClient;
    public MutableLiveData<Map<String, SkuDetails>> skusWithSkuDetails = new MutableLiveData<>();
    public RecipeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecipeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecipeFragment newInstance(String param1, String param2) {
        RecipeFragment fragment = new RecipeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);
        setUpBilling();

        // Sets the title of the recipe fragment based on what recipe was selected
        for (int i = 0; i < Recipe.getRecipesArrayList().size(); i++) {
            // If the selected recipe matches the recipe in the recipe array list
            if (RecipeListFragment.listViewPosition == i) {
                // Get the selected recipe's name
                String recipeName = Recipe.getRecipesArrayList().get(i).getName();
                // Set the fragment title to the selected recipe's name
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(recipeName);
            }
        }


        // Create image and text views for the recipe fragment
        ImageView recipeImageSelectedImageView = view.findViewById(R.id.recipeImageViewLarge);
        TextView recipeNameSelectedTextViewBlack = view.findViewById(R.id.recipeSelectedNameTextViewBlack);

        Button ingredientsButton = view.findViewById(R.id.ingredientsButton);
        ingredientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe();
            }
        });

        Button instructionsButton = view.findViewById(R.id.instructionsButton);
        instructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_recipe_fragment_to_instructions_fragment);
            }
        });

        // Get the name and image of the recipe selected from the recipe list view using the recipe array list
        String recipeNameSelected = Recipe.getRecipesArrayList().get(RecipeListFragment.listViewPosition).getName();
        int recipeImageSelected = Recipe.getRecipesArrayList().get(RecipeListFragment.listViewPosition).getImageOfDish();

        // Set the text and image on the layout to the name and image selected
        recipeNameSelectedTextViewBlack.setText(recipeNameSelected);
        recipeImageSelectedImageView.setImageDrawable(getContext().getDrawable(recipeImageSelected));

        return view;
    }
    private void setUpBilling() {
        billingClient = BillingClient.newBuilder(getActivity())
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull @NotNull BillingResult billingResult, @Nullable @org.jetbrains.annotations.Nullable List<Purchase> list) {

                    }
                })
                .enablePendingPurchases()
                .build();

        if (!billingClient.isReady()) {
            Log.d(TAG, "BillingClient: Start connection...");
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingServiceDisconnected() {
                    Log.d(TAG, "onBillingServiceDisconnected");

                }

                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    int responseCode = billingResult.getResponseCode();
                    String debugMessage = billingResult.getDebugMessage();
                    Log.d(TAG, "onBillingSetupFinished: " + responseCode + " " + debugMessage);
                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                        // The billing client is ready. You can query purchases here.
                        querySkuDetails();
                        queryPurchase();
                    }
                }
            });
        }
    }
    private void subscribe() {
        if(billingClient.isReady()){
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(LIST_OF_SKUS)
                    .setType(BillingClient.SkuType.SUBS)
                    .build();

            Log.d(TAG, "subscribe:  subscribed");

            billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                    Log.d(TAG, "onSkuDetailsResponse: Begin sss");
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                        Log.d(TAG, "onSkuDetailsResponse: everything is ok");

                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(list.get(0))
                                .build();
                        Log.d(TAG, "onSkuDetailsResponse: start billing");

                        int response = billingClient.launchBillingFlow(getActivity(),billingFlowParams)
                                .getResponseCode();
                        switch (response)
                        {
                            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                                Toast.makeText(getContext(),"Billing unavilable ",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                                Toast.makeText(getContext(),"item unavilable ",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                                Toast.makeText(getContext(),"Developer Error  ",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                                Toast.makeText(getContext(),"Feature not supported",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                                Toast.makeText(getContext(),"ITEM OWNED  ",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                                Toast.makeText(getContext(),"SERVICE ISCONNTETD ",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                                Toast.makeText(getContext(),"TIME OUT",Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                break;

                        }

                    }
                }
            });
        }
    }
    private void queryPurchase() {
        if (!billingClient.isReady()) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready");
        }
        Log.d(TAG, "queryPurchases: SUBS");
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {


            }
        });
    }

    private void querySkuDetails() {
        SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.SUBS)
                .setSkusList(LIST_OF_SKUS)
                .build();
        Log.i(TAG, "querySkuDetailsAsync");

        billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                if (billingResult == null) {
                    Log.wtf(TAG, "onSkuDetailsResponse: null BillingResult");
                }

                int responseCode = billingResult.getResponseCode();
                String debugMessage = billingResult.getDebugMessage();

                switch (responseCode) {
                    case BillingClient.BillingResponseCode.OK:
                        Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        final int expectedSkuDetailsCount = LIST_OF_SKUS.size();
                        if (list == null) {
                            skusWithSkuDetails.postValue(Collections.<String, SkuDetails>emptyMap());

                            Log.e(TAG, "onSkuDetailsResponse: " +
                                    "Expected " + expectedSkuDetailsCount + ", " +
                                    "Found null SkuDetails. " +
                                    "Check to see if the SKUs you requested are correctly published " +
                                    "in the Google Play Console.");
                        } else {
                            Map<String, SkuDetails> newSkusDetailList = new HashMap<String, SkuDetails>();
                            for (SkuDetails skuDetails : list) {
                                newSkusDetailList.put(skuDetails.getSku(), skuDetails);
                            }
                            skusWithSkuDetails.postValue(newSkusDetailList);
                            int skuDetailsCount = newSkusDetailList.size();
                            if (skuDetailsCount == expectedSkuDetailsCount) {
                                Log.i(TAG, "onSkuDetailsResponse: Found " + skuDetailsCount + " SkuDetails");
                            } else {
                                Log.e(TAG, "onSkuDetailsResponse: " +
                                        "Expected " + expectedSkuDetailsCount + ", " +
                                        "Found " + skuDetailsCount + " SkuDetails. " +
                                        "Check to see if the SKUs you requested are correctly published " +
                                        "in the Google Play Console.");
                            }
                        }
                        break;
                    case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                    case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                    case BillingClient.BillingResponseCode.ERROR:
                        Log.e(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                    case BillingClient.BillingResponseCode.USER_CANCELED:
                        Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                    // These response codes are not expected.
                    case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                    case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                    case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                    default:
                        Log.wtf(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                }
            }
        });
    }

    @Override
    public void onPurchasesUpdated(@NonNull @NotNull BillingResult billingResult, @Nullable @org.jetbrains.annotations.Nullable List<Purchase> list) {

    }
    public int launchBillingFlow(Activity activity, BillingFlowParams params) {
        if (!billingClient.isReady()) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready");
        }
        BillingResult billingResult = billingClient.launchBillingFlow(activity, params);
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        Log.d(TAG, "launchBillingFlow: BillingResponse " + responseCode + " " + debugMessage);
        return responseCode;
    }
}