package com.servicexpress;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    private TextView titleText;
    private TextView descriptionText;
    private View[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        titleText = findViewById(R.id.title_text);
        descriptionText = findViewById(R.id.description_text);
        
        // Enable layout transition animations
        ViewGroup textContainer = findViewById(R.id.text_container);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        textContainer.setLayoutTransition(layoutTransition);

        // Initialize dots array
        dots = new View[3];
        dots[0] = findViewById(R.id.dot_1);
        dots[1] = findViewById(R.id.dot_2);
        dots[2] = findViewById(R.id.dot_3);

        ViewPager2 carousel = findViewById(R.id.carousel);

        // Set initial dots
        updateDots(0);

        carousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTexts(position);
                updateDots(position);
            }
        });

        Button nextButton = findViewById(R.id.next_button);
        Button skipButton = findViewById(R.id.skip_button);

        List<Integer> images = Arrays.asList(
                R.drawable.placeholder,
                R.drawable.placeholder,
                R.drawable.placeholder
        );

        carousel.setAdapter(new CarouselAdapter(images));

        // Handle next button clicks
        nextButton.setOnClickListener(v -> {
            int currentItem = carousel.getCurrentItem();
            if (currentItem < images.size() - 1) {
                // Go to next page if not on last page
                carousel.setCurrentItem(currentItem + 1);
            } else {
                // On last page, start AuthGateActivity
                Intent intent = new Intent(OnboardingActivity.this, AuthGateActivity.class);
                startActivity(intent);
            }
        });

        // Handle skip button clicks
        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, AuthGateActivity.class);
            startActivity(intent);
        });
    }

    private static class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ImageViewHolder> {
        private final List<Integer> imageList;

        public CarouselAdapter(List<Integer> imageList) {
            this.imageList = imageList;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_onboarding_carousel, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            holder.imageView.setImageResource(imageList.get(position));
        }

        @Override
        public int getItemCount() {
            return imageList.size();
        }

        static class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.carousel_image);
            }
        }
    }

    private void updateTexts(int position) {
        // Create fade out animation
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(150);
        
        // Create fade in animation
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(150);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Update text when fade out completes
                switch (position) {
                    case 0:
                        titleText.setText("Find Trusted Service,\nAnytime, Anywhere.");
                        descriptionText.setText("Discover a world of reliable service\nproviders at your fingertips.");
                        break;
                    case 1:
                        titleText.setText("Explore Services\nTailored to Your Needs");
                        descriptionText.setText("Easily Browse a wide range of\ncategories and find professionals suited to\nyour requirements.");
                        break;
                    case 2:
                        titleText.setText("Book Services in\nMinutes");
                        descriptionText.setText("Enjoy a simple and secure booking process,\nSelect your service, schedule a time, and confirm\nrequest with just a few taps.");
                        break;
                }
                
                // Start fade in animation
                titleText.startAnimation(fadeIn);
                descriptionText.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // Start fade out animation
        titleText.startAnimation(fadeOut);
        descriptionText.startAnimation(fadeOut);
    }

    private void updateDots(int currentPosition) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(i == currentPosition ? 
                R.drawable.circle : R.drawable.circle_unselected);
        }
    }
}
