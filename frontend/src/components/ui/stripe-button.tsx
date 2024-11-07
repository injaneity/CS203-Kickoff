import { useEffect } from 'react'

declare global {
  namespace JSX {
    interface IntrinsicElements {
      'stripe-buy-button': React.DetailedHTMLProps<
        React.HTMLAttributes<HTMLElement> & {
          'buy-button-id': string
          'publishable-key': string
          'client-reference-id': string
        },
        HTMLElement
      >
    }
  }
}

interface StripeButtonProps {
  tournamentId: number;
}

export const StripeButton: React.FC<StripeButtonProps> = ({ tournamentId }) => {
  useEffect(() => {
    const script = document.createElement('script')
    script.src = 'https://js.stripe.com/v3/buy-button.js'
    script.async = true
    document.body.appendChild(script)

    return () => {
      document.body.removeChild(script)
    }
  }, [])

  return (
    <stripe-buy-button
      buy-button-id="buy_btn_1QHx6wLTGpFrYyLmxFtwauPW"
      publishable-key="pk_test_51MqiRWLTGpFrYyLm9jKP4mK9HdF52Sx5Y4kBOtVPYn9wWjwn0izDClnksVxd2JbJUvolYWOc6BSSBwhKxzZShOkv00DHiCoado"
      client-reference-id={tournamentId.toString()}
    />
  )
} 